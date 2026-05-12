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

package io.jmix.texttodata.introspection.search;

import io.jmix.texttodata.introspection.model.EntityDescriptor;
import io.jmix.texttodata.introspection.model.EntityPropertyDescriptor;
import io.jmix.texttodata.introspection.model.EnumPropertyDescriptor;
import io.jmix.texttodata.introspection.model.EnumValueDescriptor;
import io.jmix.texttodata.introspection.model.RelationPropertyDescriptor;
import io.jmix.texttodata.introspection.registry.DomainModelRegistry;
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

@Component("textdt_DomainModelSearchService")
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
    protected DomainModelRegistry domainModelRegistry;

    protected Analyzer analyzer;
    protected Directory directory;
    protected IndexSearcher indexSearcher;

    @PostConstruct
    public void init() {
        analyzer = new StandardAnalyzer();
        directory = new ByteBuffersDirectory();
        rebuildIndex();
    }

    public List<DomainModelSearchCandidate> search(String text) {
        return search(text, DEFAULT_LIMIT);
    }

    public List<DomainModelSearchCandidate> search(String text, int limit) {
        List<String> tokens = tokenize(text);
        if (tokens.isEmpty() || limit <= 0) {
            return List.of();
        }

        try {
            List<DomainModelSearchCandidate> candidates = new ArrayList<>();
            for (ScoreDoc scoreDoc : searchIndex(tokens, limit)) {
                Document document = indexSearcher.storedFields().document(scoreDoc.doc);
                EntityDescriptor entityDescriptor = domainModelRegistry.getEntityDescriptor(document.get(STORED_ENTITY_NAME_FIELD));
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

    protected void rebuildIndex() {
        try (IndexWriter indexWriter = new IndexWriter(directory, new IndexWriterConfig(analyzer))) {
            indexWriter.deleteAll();
            for (EntityDescriptor entityDescriptor : domainModelRegistry.getEntityDescriptors()) {
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

    protected Query createTextQuery(List<String> tokens) {
        Map<String, Float> boosts = new HashMap<>();
        boosts.put(ENTITY_NAME_FIELD, 10.0f);
        boosts.put(ENTITY_CAPTION_FIELD, 8.5f);
        boosts.put(PROPERTY_NAME_FIELD, 5.5f);
        boosts.put(PROPERTY_CAPTION_FIELD, 5.0f);
        boosts.put(PROPERTY_COMMENT_FIELD, 2.5f);
        boosts.put(RELATION_TARGET_FIELD, 3.5f);
        boosts.put(RELATION_TARGET_CAPTION_FIELD, 3.0f);
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

    protected void collectMatches(String token, Collection<String> values, String matchLabel, Set<String> matchedBy) {
        for (String value : values) {
            collectMatches(token, value, matchLabel, matchedBy);
        }
    }

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

    protected String normalize(String value) {
        return value.toLowerCase(Locale.ROOT);
    }
}
