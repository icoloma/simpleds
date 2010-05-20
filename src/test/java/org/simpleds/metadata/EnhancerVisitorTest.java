package org.simpleds.metadata;

import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.ASMifierClassVisitor;
import org.objectweb.asm.util.TraceClassVisitor;
import org.simpleds.EntityManagerFactory;
import org.simpleds.annotations.One;
import org.simpleds.metadata.asm.EnhancerVisitor;
import org.simpleds.metadata.asm.ExtensibleClassLoader;
import org.simpleds.test.AbstractDatastoreTest;
import org.simpleds.testdb.Dummy1;
import org.simpleds.testdb.Dummy2;

import com.google.appengine.api.datastore.Key;

public class EnhancerVisitorTest extends AbstractDatastoreTest {

	private EnhancerVisitor visitor;
	
	@Before
	public void setupEntityManager() {
		PersistenceMetadataRepositoryFactory factory = new PersistenceMetadataRepositoryFactory();
		factory.setLocations(new String[0]);
		PersistenceMetadataRepository repository = factory.initialize();
		repository.add(Dummy1.class);
		EntityManagerFactory emFactory = new EntityManagerFactory();
		emFactory.setPersistenceMetadataRepository(repository);
		emFactory.setDatastoreService(datastoreService);
		emFactory.initialize();
	}
	
	@Test
	public void testEnhance() throws Exception {
		Class<?> clazz = BeforeClass.class;
		ClassReader classReader = new ClassReader(clazz.getName());
		ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		visitor = new EnhancerVisitor(classWriter);
		classReader.accept(visitor, 0);
		byte[] output = classWriter.toByteArray();
		assertTrue(visitor.wasEnhanced());
		
		// visual check of the class contents
		System.out.println("****************\n Current generated class\n****************\n");
		ClassReader classReader2 = new ClassReader(output);
		classReader2.accept(new TraceClassVisitor(new PrintWriter(System.out)), 0);
		
		// test the class runtime
		Class<BeforeClass> c = (Class<BeforeClass>) ExtensibleClassLoader.instance.defineClass(clazz.getName() + "__ds__", output);
		c.newInstance().getFoo();
	}
	
	/**
	 * This method prints the expected ASM code
	 */
	@Test
	public void showExpectedOutput() throws Exception {
		System.out.println("****************\n Expected result\n****************\n");
		Class<?> clazz = AfterClass.class;
		ClassReader classReader = new ClassReader(clazz.getName());
		classReader.accept(new ASMifierClassVisitor(new PrintWriter(System.out)), 0);
	}
	
	
	public static class BeforeClass {
		
		protected Key fooKey;
		
		protected Long barId;

		protected BeforeClass() throws Exception {
		}
		
		public void getXXX() {
		}
		
		@One
		public Dummy1 getFoo() {
			throw new UnsupportedOperationException();
		}
		
		@One("barId")
		public Dummy2 getBar() {
			throw new UnsupportedOperationException();
		}

		public Key getFooKey() {
			return fooKey;
		}

		public Long getBarId() {
			return barId;
		}

		public void setBarId(Long barId) {
			this.barId = barId;
		}

		public void setFooKey(Key fooKey) {
			this.fooKey = fooKey;
		}
		
	}
	
	public static class AfterClass extends BeforeClass {
		
		protected AfterClass() throws Exception {
		}

		@Override
		public Dummy1 getFoo() {
			return EntityManagerFactory.getEntityManager().get(fooKey);
		}
		
		/*
		@Override
		public Dummy2 getBar() {
			EntityManager entityManager = EntityManagerFactory.getEntityManager();
			ClassMetadata metadata = entityManager.getClassMetadata(Dummy2.class);
			Key barKey = KeyFactory.createKey(metadata.getKind(), barId);
			return entityManager.get(barKey);
		}*/
		
	}
}
