CREATE VECTOR INDEX `knowledge-embeddings`
FOR (n:KnowledgeObject )
ON (n.embedding)
OPTIONS {indexConfig: {
 `vector.dimensions`: 1536,
 `vector.similarity_function`: 'cosine'
}}
