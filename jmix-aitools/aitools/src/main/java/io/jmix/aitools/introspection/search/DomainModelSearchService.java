/*
 * Copyright 2026 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.aitools.introspection.search;

import io.jmix.aitools.introspection.introspector.JpaDomainModelIntrospector;
import io.jmix.aitools.introspection.model.EntityDescriptor;
import io.jmix.aitools.introspection.model.EntityPropertyDescriptor;
import io.jmix.aitools.introspection.model.EnumPropertyDescriptor;
import io.jmix.aitools.introspection.model.EnumValueDescriptor;
import io.jmix.aitools.introspection.model.RelationPropertyDescriptor;
import io.jmix.aitools.dataload.prompt.PromptContextBuilder;
import jakarta.annotation.PostConstruct;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.index.Term;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Builds and queries an in-memory Lucene index over the introspected Jmix domain model.
 * <p>
 * This service is the deterministic retrieval layer of the text-to-JPQL pipeline. It takes
 * free-form user text, searches through collected entity metadata and returns a ranked
 * shortlist of candidate entities that are likely relevant for the future JPQL query.
 * <p>
 * The index is built from {@link EntityDescriptor} metadata and includes entity names,
 * localized captions, property names, property captions, comments, relation targets and
 * enum values. During search the service combines boosted lexical queries with a lighter
 * n-gram fallback query so that we keep strong exact matches while still tolerating some
 * wording and inflection differences.
 * <p>
 * Typical usage:
 * <ul>
 *     <li>call {@link #search(String)} to get the default-size ranked shortlist</li>
 *     <li>call {@link #search(String, int)} when the caller needs a custom number of candidates</li>
 *     <li>pass the resulting candidates to prompt-context building before LLM generation</li>
 * </ul>
 * The service does not call an LLM and does not mutate the source metadata. Its role is to
 * narrow the domain model search space before the generation and validation stages.
 *
 * @see PromptContextBuilder
 */
@Component("aitols_DomainModelSearchService")
public class DomainModelSearchService {

    protected static final String ENTITY_NAME_FIELD = "entityName";
    protected static final String ENTITY_CAPTION_FIELD = "entityCaption";
    protected static final String PROPERTY_NAME_FIELD = "propertyName";
    protected static final String PROPERTY_CAPTION_FIELD = "propertyCaption";
    protected static final String PROPERTY_COMMENT_FIELD = "propertyComment";
    protected static final String RELATION_TARGET_FIELD = "relationTarget";
    protected static final String RELATION_TARGET_CAPTION_FIELD = "relationTargetCaption";
    protected static final String ENUM_TYPE_FIELD = "enumType";
    protected static final String ENUM_VALUE_FIELD = "enumValue";
    protected static final String ENUM_VALUE_CAPTION_FIELD = "enumValueCaption";
    protected static final String NGRAM_FIELD = "ngrams";
    protected static final String STORED_ENTITY_NAME_FIELD = "storedEntityName";

    protected static final String[] SEARCH_FIELDS = {
            ENTITY_NAME_FIELD,
            ENTITY_CAPTION_FIELD,
            PROPERTY_NAME_FIELD,
            PROPERTY_CAPTION_FIELD,
            PROPERTY_COMMENT_FIELD,
            RELATION_TARGET_FIELD,
            RELATION_TARGET_CAPTION_FIELD,
            ENUM_TYPE_FIELD,
            ENUM_VALUE_FIELD,
            ENUM_VALUE_CAPTION_FIELD
    };

    protected static final Pattern TOKEN_SPLIT_PATTERN = Pattern.compile("[^\\p{IsAlphabetic}\\p{IsDigit}_]+");
    protected static final int DEFAULT_LIMIT = 10;
    protected static final int NGRAM_MIN_LENGTH = 3;
    protected static final int NGRAM_MAX_LENGTH = 5;

    @Autowired
    protected JpaDomainModelIntrospector modelIntrospector;

    protected Analyzer analyzer;
    protected Directory directory;
    protected IndexSearcher indexSearcher;

    @PostConstruct
    public void init() {
        analyzer = new StandardAnalyzer();
        directory = new ByteBuffersDirectory();
        // Build an in-memory Lucene index once from the current metadata snapshot.
        // The snapshot itself is already filtered by the introspector, so only
        // application-relevant entities end up in the index.
        rebuildIndex();
    }

    /**
     * Runs the user text through Lucene and then map the matched documents back to {@link EntityDescriptor} objects.
     * It uses default limitation for the number of candidates ({@link #DEFAULT_LIMIT}).
     *
     * @param text user text
     * @return list of most relevant candidates
     */
    public List<DomainModelSearchCandidate> search(String text) {
        return search(text, DEFAULT_LIMIT);
    }

    /**
     * Runs the user text through Lucene and then map the matched documents back to {@link EntityDescriptor} objects.
     *
     * @param text  user text
     * @param limit number of candidates to return
     * @return list of most relevant candidates
     */
    public List<DomainModelSearchCandidate> search(String text, int limit) {
        List<String> tokens = tokenize(text);
        if (tokens.isEmpty() || limit <= 0) {
            return List.of();
        }

        try {
            List<DomainModelSearchCandidate> candidates = new ArrayList<>();
            for (ScoreDoc scoreDoc : searchIndex(tokens, limit)) {
                Document document = indexSearcher.storedFields().document(scoreDoc.doc);
                EntityDescriptor entityDescriptor = modelIntrospector.getEntityDescriptor(document.get(STORED_ENTITY_NAME_FIELD));
                if (entityDescriptor != null) {
                    candidates.add(new DomainModelSearchCandidate(
                            entityDescriptor,
                            Math.round(scoreDoc.score * 1000),
                            collectMatchedBy(entityDescriptor, tokens)
                    ));
                }
            }

            return Collections.unmodifiableList(candidates);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to search Lucene index", e);
        }
    }

    /**
     * Rebuilds the whole in-memory Lucene index from the current registry snapshot.
     * <p>
     * The current implementation recreates the index from scratch because this keeps
     * the retrieval layer simple and deterministic, which is enough for the present
     * in-memory MVP.
     */
    protected void rebuildIndex() {
        if (!modelIntrospector.isInitialized()) {
            modelIntrospector.introspect();
        }

        try (IndexWriter indexWriter = new IndexWriter(directory, new IndexWriterConfig(analyzer))) {
            indexWriter.deleteAll();
            for (EntityDescriptor entityDescriptor : modelIntrospector.getEntityDescriptors()) {
                indexWriter.addDocument(toDocument(entityDescriptor));
            }
            indexWriter.commit();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to build Lucene index", e);
        }

        try {
            IndexReader indexReader = DirectoryReader.open(directory);
            indexSearcher = new IndexSearcher(indexReader);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to open Lucene index", e);
        }
    }

    /**
     * Executes the prepared Lucene query against the in-memory index.
     * <p>
     * The search combines a regular boosted full-text query with a lower-weight
     * character n-gram query. The full-text part gives better precision, while
     * n-grams make retrieval less brittle for word forms, inflections and slightly
     * different naming.
     *
     * @param tokens list of tokens retrieved from user text
     * @param limit number of candidates to return
     * @return matched Lucene documents with their scores
     * @throws IOException if index search fails with an error
     */
    protected ScoreDoc[] searchIndex(List<String> tokens, int limit) throws IOException {
        BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();

        Query textQuery = createTextQuery(tokens);
        if (textQuery != null) {
            queryBuilder.add(textQuery, BooleanClause.Occur.SHOULD);
        }

        Query ngramQuery = createNgramQuery(tokens);
        if (ngramQuery != null) {
            queryBuilder.add(new BoostQuery(ngramQuery, 0.35f), BooleanClause.Occur.SHOULD);
        }

        BooleanQuery query = queryBuilder.build();
        if (query.clauses().isEmpty()) {
            return new ScoreDoc[0];
        }

        return indexSearcher.search(query, limit).scoreDocs;
    }

    /**
     * Creates the main boosted lexical Lucene query for the provided user tokens.
     * <p>
     * This is the primary ranking query in the retrieval pipeline. Different metadata
     * fields receive different boost coefficients so that canonical entity identifiers
     * and business-facing captions rank above weaker or noisier signals such as free-form
     * comments. The resulting weighting keeps the search explainable while still allowing
     * fields like enum values and relation targets to influence ranking when they are
     * explicitly mentioned in the prompt.
     *
     * @param tokens normalized tokens extracted from user text
     * @return boosted lexical query spanning the indexed metadata fields
     */
    protected Query createTextQuery(List<String> tokens) {
        Map<String, Float> boosts = new HashMap<>();

        // Entity name is the strongest lexical signal because it is the canonical
        // identifier used in JPQL and usually the least ambiguous field.
        boosts.put(ENTITY_NAME_FIELD, 10.0f);

        // Localized entity captions are nearly as important as the technical name,
        // because real users search by business labels, not by Java naming.
        boosts.put(ENTITY_CAPTION_FIELD, 8.5f);

        // Property names and captions are weaker than entity-level matches but still
        // strong enough to pull an entity up when the user mentions a known field.
        boosts.put(PROPERTY_NAME_FIELD, 5.5f);
        boosts.put(PROPERTY_CAPTION_FIELD, 5.0f);

        // Comments are useful hints, but they are free-form text and therefore much
        // noisier than names and captions.
        boosts.put(PROPERTY_COMMENT_FIELD, 2.5f);

        // Relation target names/captions help queries like "orders by customer", but
        // they should not outrank direct matches on the entity itself.
        boosts.put(RELATION_TARGET_FIELD, 3.5f);
        boosts.put(RELATION_TARGET_CAPTION_FIELD, 3.0f);

        // Enum metadata matters for queries like "open orders", yet enum type names
        // are usually weaker than enum values or main entity/property names.
        boosts.put(ENUM_TYPE_FIELD, 2.0f);
        boosts.put(ENUM_VALUE_FIELD, 4.0f);
        boosts.put(ENUM_VALUE_CAPTION_FIELD, 3.5f);

        try {
            MultiFieldQueryParser parser = new MultiFieldQueryParser(SEARCH_FIELDS, analyzer, boosts);
            return parser.parse(tokens.stream()
                    .map(MultiFieldQueryParser::escape)
                    .collect(Collectors.joining(" ")));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse Lucene query", e);
        }
    }

    /**
     * Creates a softer n-gram-based fallback query for the provided user tokens.
     * <p>
     * Character n-grams improve recall for different word forms and partially
     * overlapping tokens without introducing a full stemming or morphology layer.
     *
     * @param tokens normalized tokens extracted from user text
     * @return n-gram fallback query or {@code null} if there are no usable n-grams
     */
    protected Query createNgramQuery(List<String> tokens) {
        Set<String> ngrams = new LinkedHashSet<>();
        for (String token : tokens) {
            ngrams.addAll(generateNgrams(token));
        }
        if (ngrams.isEmpty()) {
            return null;
        }

        BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
        for (String ngram : ngrams) {
            queryBuilder.add(new TermQuery(new Term(NGRAM_FIELD, ngram)), BooleanClause.Occur.SHOULD);
        }
        return queryBuilder.build();
    }

    /**
     * Converts one entity descriptor into a single Lucene document.
     * <p>
     * Each document stores multiple views of the same entity metadata, so Lucene can
     * match user text against names, localized captions, comments, relation targets,
     * and enum values while still returning one entity-level hit.
     *
     * @param entityDescriptor source entity metadata
     * @return Lucene document representing the entity
     */
    protected Document toDocument(EntityDescriptor entityDescriptor) {
        Document document = new Document();
        document.add(new StoredField(STORED_ENTITY_NAME_FIELD, entityDescriptor.getName()));

        addValues(document, ENTITY_NAME_FIELD, List.of(entityDescriptor.getName()));
        addValues(document, ENTITY_CAPTION_FIELD, entityDescriptor.getLocalizedNames());

        Set<String> ngrams = new LinkedHashSet<>(generateNgrams(entityDescriptor.getName()));
        entityDescriptor.getLocalizedNames().forEach(value -> ngrams.addAll(generateNgrams(value)));

        for (EntityPropertyDescriptor propertyDescriptor : entityDescriptor.getProperties()) {
            addValues(document, PROPERTY_NAME_FIELD, List.of(propertyDescriptor.getName()));
            addValues(document, PROPERTY_CAPTION_FIELD, propertyDescriptor.getLocalizedNames());
            addValues(document, PROPERTY_COMMENT_FIELD, propertyDescriptor.getComment() == null
                    ? List.of()
                    : List.of(propertyDescriptor.getComment()));

            ngrams.addAll(generateNgrams(propertyDescriptor.getName()));
            propertyDescriptor.getLocalizedNames().forEach(value -> ngrams.addAll(generateNgrams(value)));
            if (propertyDescriptor.getComment() != null) {
                ngrams.addAll(generateNgrams(propertyDescriptor.getComment()));
            }

            if (propertyDescriptor instanceof RelationPropertyDescriptor relationPropertyDescriptor) {
                addValues(document, RELATION_TARGET_FIELD, List.of(relationPropertyDescriptor.getTargetEntityName()));
                addValues(document, RELATION_TARGET_CAPTION_FIELD, relationPropertyDescriptor.getTargetEntityLocalizedNames());

                ngrams.addAll(generateNgrams(relationPropertyDescriptor.getTargetEntityName()));
                relationPropertyDescriptor.getTargetEntityLocalizedNames().forEach(value -> ngrams.addAll(generateNgrams(value)));
            }

            if (propertyDescriptor instanceof EnumPropertyDescriptor enumPropertyDescriptor) {
                addValues(document, ENUM_TYPE_FIELD, List.of(enumPropertyDescriptor.getEnumType().getName()));
                ngrams.addAll(generateNgrams(enumPropertyDescriptor.getEnumType().getName()));

                for (EnumValueDescriptor enumValueDescriptor : enumPropertyDescriptor.getEnumType().getConstants().values()) {
                    addValues(document, ENUM_VALUE_FIELD, List.of(enumValueDescriptor.getName()));
                    addValues(document, ENUM_VALUE_CAPTION_FIELD, enumValueDescriptor.getLocalizedName());

                    ngrams.addAll(generateNgrams(enumValueDescriptor.getName()));
                    enumValueDescriptor.getLocalizedName().forEach(value -> ngrams.addAll(generateNgrams(value)));
                }
            }
        }

        for (String ngram : ngrams) {
            document.add(new TextField(NGRAM_FIELD, ngram, Field.Store.NO));
        }

        return document;
    }

    protected void addValues(Document document, String fieldName, Collection<String> values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                document.add(new TextField(fieldName, value, Field.Store.NO));
            }
        }
    }

    // TODO: pinyazhin, leave for debug? Enable it using app property?
    /**
     * Collects lightweight match labels for diagnostics and tests.
     * <p>
     * Real ranking comes from Lucene scores, but the returned {@code matchedBy} labels
     * make the search result easier to inspect during debugging and in unit tests.
     *
     * @param entityDescriptor candidate entity
     * @param tokens normalized user tokens
     * @return explanation labels describing which metadata fragments matched
     */
    protected List<String> collectMatchedBy(EntityDescriptor entityDescriptor, List<String> tokens) {
        Set<String> matchedBy = new LinkedHashSet<>();

        for (String token : tokens) {
            collectMatches(token, entityDescriptor.getName(), "entityName", matchedBy);
            collectMatches(token, entityDescriptor.getLocalizedNames(), "entityCaption", matchedBy);

            for (EntityPropertyDescriptor propertyDescriptor : entityDescriptor.getProperties()) {
                collectMatches(token, propertyDescriptor.getName(), "property:" + propertyDescriptor.getName(), matchedBy);
                collectMatches(token, propertyDescriptor.getLocalizedNames(),
                        "propertyCaption:" + propertyDescriptor.getName(), matchedBy);
                collectMatches(token, propertyDescriptor.getComment(),
                        "propertyComment:" + propertyDescriptor.getName(), matchedBy);

                if (propertyDescriptor instanceof RelationPropertyDescriptor relationPropertyDescriptor) {
                    collectMatches(token, relationPropertyDescriptor.getTargetEntityName(),
                            "relationTarget:" + propertyDescriptor.getName(), matchedBy);
                    collectMatches(token, relationPropertyDescriptor.getTargetEntityLocalizedNames(),
                            "relationTargetCaption:" + propertyDescriptor.getName(), matchedBy);
                }

                if (propertyDescriptor instanceof EnumPropertyDescriptor enumPropertyDescriptor) {
                    collectMatches(token, enumPropertyDescriptor.getEnumType().getName(),
                            "enumType:" + propertyDescriptor.getName(), matchedBy);
                    for (EnumValueDescriptor enumValueDescriptor : enumPropertyDescriptor.getEnumType().getConstants().values()) {
                        collectMatches(token, enumValueDescriptor.getName(),
                                "enumValue:" + propertyDescriptor.getName(), matchedBy);
                        collectMatches(token, enumValueDescriptor.getLocalizedName(),
                                "enumValueCaption:" + propertyDescriptor.getName(), matchedBy);
                    }
                }
            }
        }

        return List.copyOf(matchedBy);
    }

    /**
     * Applies the same lightweight explanation matching rule to a collection of values.
     *
     * @param token normalized search token
     * @param values candidate values to inspect
     * @param matchLabel label to record when a match is found
     * @param matchedBy mutable set of collected explanation labels
     */
    protected void collectMatches(String token, Collection<String> values, String matchLabel, Set<String> matchedBy) {
        for (String value : values) {
            collectMatches(token, value, matchLabel, matchedBy);
        }
    }

    /**
     * Applies a simple explanation-only matching rule to one value.
     * <p>
     * This logic is intentionally simpler than the real Lucene ranking because
     * {@code matchedBy} is only an explanation layer, not the ranking engine itself.
     *
     * @param token normalized search token
     * @param value candidate value to inspect
     * @param matchLabel label to record when a match is found
     * @param matchedBy mutable set of collected explanation labels
     */
    protected void collectMatches(String token, String value, String matchLabel, Set<String> matchedBy) {
        if (value == null || value.isBlank()) {
            return;
        }

        String normalizedValue = normalize(value);
        if (normalizedValue.isEmpty()) {
            return;
        }

        if (normalizedValue.equals(token) || normalizedValue.contains(token)) {
            matchedBy.add(matchLabel);
        }
    }

    /**
     * Generates character n-grams for the provided text value.
     * <p>
     * The produced n-grams are indexed in a dedicated Lucene field and used only as
     * a soft recall booster.
     *
     * @param value source text
     * @return generated character n-grams
     */
    protected Set<String> generateNgrams(String value) {
        List<String> tokens = tokenize(value);
        Set<String> ngrams = new LinkedHashSet<>();
        for (String token : tokens) {
            if (token.length() < NGRAM_MIN_LENGTH) {
                if (!token.isBlank()) {
                    ngrams.add(token);
                }
                continue;
            }

            int maxLength = Math.min(NGRAM_MAX_LENGTH, token.length());
            for (int size = NGRAM_MIN_LENGTH; size <= maxLength; size++) {
                for (int index = 0; index <= token.length() - size; index++) {
                    ngrams.add(token.substring(index, index + size));
                }
            }
        }
        return ngrams;
    }

    /**
     * Splits text into normalized lexical tokens.
     * <p>
     * Tokenization is shared by both index-building support code and query preparation
     * so that both sides use the same basic normalization rules.
     *
     * @param text source text
     * @return normalized nonblank tokens
     */
    protected List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        List<String> tokens = new ArrayList<>();
        for (String token : TOKEN_SPLIT_PATTERN.split(text.toLowerCase(Locale.ROOT))) {
            if (!token.isBlank()) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    /**
     * Normalizes text for lightweight explanation matching.
     * <p>
     * Lowercase normalization is enough for the current Lucene-based MVP. More
     * advanced normalization can be added later if retrieval needs it.
     *
     * @param value source text
     * @return normalized value
     */
    protected String normalize(String value) {
        return value.toLowerCase(Locale.ROOT);
    }
}
