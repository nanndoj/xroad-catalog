package fi.vrk.xroad.catalog.persistence.entity;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@ToString(exclude = "subsystems")
// Entity graph defining tree member-subsystem-service.
@NamedEntityGraph(
        name = "member.full-tree.graph",
        attributeNodes = {
                @NamedAttributeNode(value = "subsystems", subgraph = "subsystem.services.graph")
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "subsystem.services.graph",
                        attributeNodes = @NamedAttributeNode(value = "services", subgraph = "service.wsdl.graph")),
                @NamedSubgraph(
                        name = "service.wsdl.graph",
                        attributeNodes = @NamedAttributeNode(value = "wsdl")),
        }
)
@NamedQueries({
        // query fetches all members that have been changed, or have child entities
        // (subsystems, services, wsdls) that have been changed since given date
        @NamedQuery(name = "Member.findAllChangedSince",
                query = Member.FIND_ALL_CHANGED_QUERY),
        @NamedQuery(name = "Member.findActiveChangedSince",
                query = Member.FIND_ACTIVE_CHANGED_QUERY),
})
public class Member {

    // TODO: would be nice to make less ugly
    private static final String FIND_CHANGED_QUERY_PART_1 =
            "SELECT DISTINCT mem " +
                    "FROM Member mem " +
                    "LEFT JOIN FETCH mem.subsystems fetchedSubs " +
                    "LEFT JOIN FETCH fetchedSubs.services fetchedSers " +
                    "LEFT JOIN FETCH fetchedSers.wsdl fetchedWsdl " +
                    "WHERE ";
    private static final String FIND_CHANGED_QUERY_PART_2 =
            "mem.statusInfo.changed > :since " +
                    "OR EXISTS ( " +
                    "SELECT sub " +
                    "FROM Subsystem sub " +
                    "WHERE sub.member = mem " +
                    "AND sub.statusInfo.changed > :since)" +
                    "OR EXISTS ( " +
                    "SELECT service " +
                    "FROM Service service " +
                    "WHERE service.subsystem.member = mem " +
                    "AND service.statusInfo.changed > :since)" +
                    "OR EXISTS ( " +
                    "SELECT wsdl " +
                    "FROM Wsdl wsdl " +
                    "WHERE wsdl.service.subsystem.member = mem " +
                    "AND wsdl.statusInfo.changed > :since) ";
    static final String FIND_ALL_CHANGED_QUERY =
            FIND_CHANGED_QUERY_PART_1 + FIND_CHANGED_QUERY_PART_2;
    static final String FIND_ACTIVE_CHANGED_QUERY =
            FIND_CHANGED_QUERY_PART_1 +
            "mem.statusInfo.removed IS NULL AND (" +
            FIND_CHANGED_QUERY_PART_2 +
            ")";
    static {
        System.out.println(FIND_ACTIVE_CHANGED_QUERY);
    }

    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MEMBER_GEN")
    @SequenceGenerator(name = "MEMBER_GEN", sequenceName = "MEMBER_ID_SEQ", allocationSize = 1)
    private long id;
    private String xRoadInstance;
    private String memberClass;
    private String memberCode;
    private String name;
    @Embedded
    private StatusInfo statusInfo = new StatusInfo();
    @Getter(AccessLevel.NONE) // do not create default getter, we provide the substitute
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private Set<Subsystem> subsystems;

    /**
     * Updates data with values from a transient non-deleted Member object,
     * and sets all data fields accordingly
     *
     * @param transientMember
     * @param timestamp
     */
    public void updateWithDataFrom(Member transientMember, Date timestamp) {
        assert transientMember.getStatusInfo().getRemoved() == null;
        boolean isModifiedData = !isDataIdentical(transientMember);
        name = transientMember.getName();
        statusInfo.setTimestampsForSaved(timestamp, isModifiedData);
    }

    public StatusInfo getStatusInfo() {
        return statusInfo;
    }

    /**
     * @return comparable & equals-able natural key
     */
    public MemberId createKey() {
        return new MemberId(xRoadInstance, memberClass, memberCode);
    }

    /**
     * Compares objects with just the "direct payload" - not ids, references entities or timestamps
     *
     * @param another
     * @return
     */
    private boolean isDataIdentical(Member another) {
        return ComparisonChain.start()
                .compare(this.name, another.name)
                .compare(this.xRoadInstance, another.xRoadInstance)
                .compare(this.memberClass, another.memberClass)
                .compare(this.memberCode, another.memberCode)
                .result() == 0;
    }

    public Member() {
    }

    public Member(String xRoadInstance,
                  String memberClass,
                  String memberCode,
                  String name) {
        this.xRoadInstance = xRoadInstance;
        this.memberClass = memberClass;
        this.memberCode = memberCode;
        this.name = name;
        statusInfo.setTimestampsForNew(new Date());
    }

    /**
     * Note: Read-only collection, do not use this to modify collection
     * @return
     */
    public Set<Subsystem> getActiveSubsystems() {
        return Collections.unmodifiableSet(subsystems.stream()
                .filter(subsystem -> !subsystem.getStatusInfo().isRemoved())
                .collect(Collectors.toSet()));
    }

    /**
     * This collection can be used to add new items
     * @return
     */
    public Set<Subsystem> getAllSubsystems() {
        return subsystems;
    }

}


