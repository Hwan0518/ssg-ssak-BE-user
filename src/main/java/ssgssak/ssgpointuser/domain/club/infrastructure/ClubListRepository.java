package ssgssak.ssgpointuser.domain.club.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ssgssak.ssgpointuser.domain.club.entity.ClubList;

@Repository
public interface ClubListRepository extends JpaRepository<ClubList, Long>{
    ClubList findByUuid(String uuid);
}
