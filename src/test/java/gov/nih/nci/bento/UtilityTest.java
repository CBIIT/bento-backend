package gov.nih.nci.bento;

import co.elastic.clients.elasticsearch._types.SortOrder;
import gov.nih.nci.bento.utility.ElasticUtility;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class UtilityTest {

    @Test
    public void elasticUtilityTest() {
        assertThat(SortOrder.Asc,is(ElasticUtility.getSortType("Asc")));
        assertThat(SortOrder.Asc,is(ElasticUtility.getSortType("AsC")));
        assertThat(SortOrder.Desc, is(ElasticUtility.getSortType("DESC")));
        assertThat(SortOrder.Desc, is(ElasticUtility.getSortType("DSC")));
        assertThat(SortOrder.Desc, is(ElasticUtility.getSortType("none")));
        assertThat(SortOrder.Desc, is(ElasticUtility.getSortType(null)));

    }

}
