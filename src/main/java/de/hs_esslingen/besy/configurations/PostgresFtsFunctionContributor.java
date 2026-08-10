package de.hs_esslingen.besy.configurations;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.type.StandardBasicTypes;

public class PostgresFtsFunctionContributor implements FunctionContributor {

    @Override
    public void contributeFunctions(FunctionContributions fc) {
        var types = fc.getTypeConfiguration().getBasicTypeRegistry();

        // search_vector @@ websearch_to_tsquery('german', :q)
        fc.getFunctionRegistry().registerPattern(
                "fts_match",
                "(?1 @@ websearch_to_tsquery('german', ?2))",
                types.resolve(StandardBasicTypes.BOOLEAN));

        // similarity(search_text, :q)
        fc.getFunctionRegistry().registerPattern(
                "trgm_sim",
                "similarity(?1, ?2)",
                types.resolve(StandardBasicTypes.DOUBLE));
    }
}
