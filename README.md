<a name="Introduction"></a>Introduction[][]
===========================================

SimpleDS provides a simple persistence framework for Google AppEngine
that gets as little in the way as possible. It is barely a wrapper
around Datastore APIs, providing mapping between Entity and Java
classes.

You can explore the details about SimpleDS at [our presentation for
Codemotion 2012][].

There is also a [demo application at GitHub][].

<a name="Features"></a>Features[][1]
====================================

-   Supports Guice, Spring or plain Java configuration.
-   Driven by SimpleDS or JPA annotations.
-   Support for embedded classes.
-   ==, \<, \<=, \>, \<=, IN, != and left like are supported.
-   Easy transformations between java and Datastore Entities.
-   An entire set of classes to support Cursors[?][].
-   Validations of [expected ancestors][]
-   [Multiple index][] values
-   [Transactions][] support
-   Level 1 and 2 [Cache][]
-   [Background tasks][]
-   [Functions][]
-   [Cacheable queries][Cache]
-   Lots of other features to improve the [Performance][] of your
    applications

<a name="Changelog"></a>Changelog[][2]
======================================

-   [SimpleDS 1.1 announcement][]
-   [SimpleDS 1.0\_RC1 announcement][]
-   [SimpleDS 0.9 announcement][]
-   [SimpleDS 0.8.1 announcement][]
-   [The first SimpleDS release announcement][]

<a name="Examples"></a>Examples[][3]
====================================

This is a list of how to do things with SimpleDS, compared with JPA:

Handle entities:

```Java
// JPA retrieve by key
Model m1 = entityManager.find(Model.class, key);
Model m2 = entityManager.find(Model.class, key2);

// SimpleDS retrieve by key
Model m1 = entityManager.get(key);
List<Model> l = entityManager.get(key1, key2);

// JPA persist changes
entityManager.merge(m1);
entityManager.persist(m2);

// SimpleDS persist changes
entityManager.put(m1);
entityManager.put(l);
Model m3 = new Model();
entityManager.put(parentKey, m3);

// JPA remove
Model m1 = entityManager.find(Model.class, key);
entityManager.remove(m1);

// SimpleDS remove
entityManager.remove(key1, key2, key3);
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
  .lessThanOrEqual("createdAt", new Date())
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

  []: #Introduction
  [our presentation for Codemotion 2012]: http://www.slideshare.net/icoloma/codemotion-appengine
  [demo application at GitHub]: https://github.com/icoloma/simpleds-kickstart
  [1]: #Features
  [?]: /p/simpleds/w/edit/Cursors
  [expected ancestors]: /p/simpleds/wiki/ParentChild
  [Multiple index]: /p/simpleds/wiki/IndexManager
  [Transactions]: /p/simpleds/wiki/Transactions
  [Cache]: /p/simpleds/wiki/Cache
  [Background tasks]: /p/simpleds/wiki/BackgroundTasks
  [Functions]: /p/simpleds/wiki/Functions
  [Performance]: /p/simpleds/wiki/Performance
  [2]: #Changelog
  [SimpleDS 1.1 announcement]: http://groups.google.com/group/simpleds/browse_thread/thread/34b226cd094bd40f
  [SimpleDS 1.0\_RC1 announcement]: http://groups.google.com/group/google-appengine-java/browse_thread/thread/43a7d7334260ffeb
  [SimpleDS 0.9 announcement]: http://groups.google.com/group/google-appengine-java/browse_thread/thread/99c05c1beb780bfa/b8a2558890365d0c?lnk=gst&q=simpleds#b8a2558890365d0c
  [SimpleDS 0.8.1 announcement]: http://groups.google.com/group/google-appengine-java/browse_thread/thread/a84b3aaf0b097d18/af8fc3b974ffb5b7?lnk=gst&q=simpleds#af8fc3b974ffb5b7
  [The first SimpleDS release announcement]: http://groups.google.com/group/google-appengine-java/browse_thread/thread/cf34118e4f5846cb/83ee599bfd645d30?lnk=gst&q=simpleds#83ee599bfd645d30
  [3]: #Examples