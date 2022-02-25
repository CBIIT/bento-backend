package gov.nih.nci.bento;

import gov.nih.nci.bento.utility.ElasticUtil;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class UtilityTest {

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
