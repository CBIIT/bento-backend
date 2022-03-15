package model;

import gov.nih.nci.bento.config.ConfigurationDAO;
import gov.nih.nci.bento.search.datafetcher.DataFetcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
@RunWith( SpringRunner.class )
@SpringBootTest
public class ConfigurationDAOTest {

    // TODO No Bean
    @Autowired
    public ConfigurationDAO dao;

    /**
     * Confirm that the data fetcher at least return one configuration file:
     */
    @Test
    public void dataFetcher_Test() {
        DataFetcher fetcher = dao.dataFetcher();
        assertThat(fetcher).isNotNull();
    }


}