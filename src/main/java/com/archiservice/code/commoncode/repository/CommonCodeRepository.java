package com.archiservice.code.commoncode.repository;

import com.archiservice.code.commoncode.domain.CommonCode;
import com.archiservice.code.commoncode.domain.id.CommonCodeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommonCodeRepository extends JpaRepository<CommonCode, CommonCodeId> {

    @Query("SELECT cc FROM CommonCode cc WHERE cc.commonCodeId.groupCode = :groupCode AND cc.commonCodeId.commonCode = :commonCode")
    Optional<CommonCode> findByGroupCodeAndCommonCode(
            @Param("groupCode") String groupCode,
            @Param("commonCode") String commonCode);

    @Query("SELECT cc.commonCodeId.commonCode FROM CommonCode cc WHERE cc.commonCodeId.groupCode = :groupCode AND cc.commonName = :commonName")
    Optional<String> findCommonCodeByGroupCodeAndCommonName(@Param("groupCode") String groupCode, @Param("commonName") String commonName);
}
