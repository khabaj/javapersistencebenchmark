package com.khabaj.ormbenchmark.benchmarks.jpa;

import com.khabaj.ormbenchmark.benchmarks.BaseBenchmark;
import com.khabaj.ormbenchmark.benchmarks.configuration.jpa.JpaSpringConfiguration;
import com.khabaj.ormbenchmark.benchmarks.configuration.jpa.JpaVendor;
import com.khabaj.ormbenchmark.benchmarks.entities.User;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;

public abstract class JpaBenchmark extends BaseBenchmark{

    protected ConfigurableApplicationContext applicationContext;
    protected EntityManager entityManager;

    public void init(JpaVendor jpaVendor) {
        this.init(jpaVendor, BATCH_SIZE);
    }

    public void init(JpaVendor jpaVendor, int batchSize) {
        try {
            this.applicationContext = new AnnotationConfigApplicationContext(JpaSpringConfiguration.class);
            this.entityManager = applicationContext.getBean(EntityManagerFactory.class, jpaVendor, batchSize).createEntityManager();
        } catch (Exception e) {
            if (applicationContext != null) {
                applicationContext.close();
            }
            System.out.println(e.getMessage());
        }
    }

    @TearDown
    public void clear() {
        entityManager.clear();
    }

    @TearDown
    public void closeApplicationContext() {
        entityManager.close();
        applicationContext.close();
    }

    @Setup()
    public abstract void setUp();

    public void performBatchInsert(int rowsNumber) {
        performBatchInsert(rowsNumber, BATCH_SIZE);
    }

    public void performBatchInsert(int rowsNumber, int batchSize) {
        entityManager.getTransaction().begin();
        for (int i = 0; i < rowsNumber; i++) {
            if (i > 0 && i % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();

                entityManager.getTransaction().commit();
                entityManager.getTransaction().begin();
            }
            entityManager.persist(new User("John" + i, "LastName" + i));
        }
        entityManager.flush();
        entityManager.clear();
        entityManager.getTransaction().commit();
    }

    protected List<User> getUsers() {
        List<User> users = entityManager.createQuery("select u from User as u").getResultList();
        return users;
    }
}
