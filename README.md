SimpleDS provides a simple persistence framework for Google AppEngine
that gets as little in the way as possible. It is barely a wrapper
around Datastore APIs, providing mapping between Entity and Java
classes.

You can explore the details about SimpleDS at [our presentation for
Codemotion 2012](http://www.slideshare.net/icoloma/codemotion-appengine).

There is also a [demo application at GitHub](https://github.com/icoloma/simpleds-kickstart).

## Features

-   Supports Guice, Spring or plain Java configuration.
-   Support for embedded classes serialized as properties or in JSON format.
-   ==, \<, \<=, \>, \<=, IN, != and left like are supported.
-   Easy transformations between Java Objects and Datastore Entities.
-   Easy support for Cursors.
-   Validations of expected ancestors
-   Transaction support
-   Level 1 and 2 Cache
-   Cacheable queries

## Examples

This is a brief comparison between SimpleDS and JPA:

```Java
// JPA retrieve by key
Model m1 = entityManager.find(Model.class, key);
Model m2 = entityManager.find(Model.class, key2);

// SimpleDS retrieve by key
Model m1 = entityManager.get(key);
Map<Key, Model> l = entityManager.get(key1, key2);

// JPA persist changes
entityManager.merge(m1);
entityManager.persist(m2);

// SimpleDS persist changes
entityManager.put(m1);
entityManager.put(ImmutableList.of(m1, m2));
Model m3 = new Model();
entityManager.put(parentKey, m3);

// JPA remove
Model m1 = entityManager.find(Model.class, key);
entityManager.remove(m1);

// SimpleDS remove
entityManager.remove(ImmutableList.of(key1, key2, key3));
```

Queries

```Java
// JPA
Query query = entityManager.createQuery(
   "select m from Model m where m.createdAt<=?1 and m.createdBy=?"
);
query.setParameter(1, new Date());
query.setParameter(2, userKey);
return query.getResultList();

// SimpleDS
return entityManager.createQuery(Model.class)
  .lessThan("createdAt", new Date())
  .equal("createdBy", userKey)
  .asList();

// retrieve just keys
return entityManager.createQuery(Model.class)
  .keysOnly()
  .asList();

// with limits
return entityManager.createQuery(Model.class)
  .withOffset(10)
  .withLimit(100)
  .asList();
```
