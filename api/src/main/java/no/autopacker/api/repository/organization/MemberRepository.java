package no.autopacker.api.repository.organization;


import no.autopacker.api.entity.organization.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findAllByOrganization_Id(Long organizationId);
    Member findByOrganization_IdAndUser_Username(Long organizationId, String username);

}
