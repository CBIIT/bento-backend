package gov.nih.nci.bento.utility;

import org.opensearch.search.sort.SortOrder;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ElasticUtilityTest {

    @Test
    public void elasticUtilityTest() {
        assertThat(SortOrder.ASC,is(ElasticUtil.getSortType("Asc")));
        assertThat(SortOrder.ASC,is(ElasticUtil.getSortType("AsC")));
        assertThat(SortOrder.DESC, is(ElasticUtil.getSortType("DESC")));
        assertThat(SortOrder.DESC, is(ElasticUtil.getSortType("DSC")));
        assertThat(SortOrder.DESC, is(ElasticUtil.getSortType("none")));
        assertThat(SortOrder.DESC, is(ElasticUtil.getSortType(null)));
    }
}
