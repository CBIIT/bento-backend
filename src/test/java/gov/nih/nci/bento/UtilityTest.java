package gov.nih.nci.bento;

import gov.nih.nci.bento.utility.ElasticUtility;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class UtilityTest {

    @Test
    public void elasticUtilityTest() {
        assertThat(SortOrder.ASC,is(ElasticUtility.getSortType("Asc")));
        assertThat(SortOrder.ASC,is(ElasticUtility.getSortType("AsC")));
        assertThat(SortOrder.DESC, is(ElasticUtility.getSortType("DESC")));
        assertThat(SortOrder.DESC, is(ElasticUtility.getSortType("DSC")));
        assertThat(SortOrder.DESC, is(ElasticUtility.getSortType("none")));
        assertThat(SortOrder.DESC, is(ElasticUtility.getSortType(null)));
    }
}
