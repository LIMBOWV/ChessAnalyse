package org.example.stockfishanalyzer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "stockfish.engine.path=/usr/local/bin/stockfish"
})
class StockfishAnalyzerApplicationTests {

    @Test
    void contextLoads() {
    }

}
