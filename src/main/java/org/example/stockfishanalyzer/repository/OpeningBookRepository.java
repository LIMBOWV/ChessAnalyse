package org.example.stockfishanalyzer.repository;

import org.example.stockfishanalyzer.entity.OpeningBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 开局库数据访问接口
 */
@Repository
public interface OpeningBookRepository extends JpaRepository<OpeningBook, Long> {

    /**
     * 根据 ECO 代码查找开局
     */
    Optional<OpeningBook> findByEcoCode(String ecoCode);

    /**
     * 根据开局名称模糊查询
     */
    List<OpeningBook> findByOpeningNameContaining(String openingName);

    /**
     * 按流行度降序查询前 N 个开局
     */
    List<OpeningBook> findTop10ByOrderByPopularityDesc();

    /**
     * 检查 ECO 代码是否存在
     */
    boolean existsByEcoCode(String ecoCode);
}

