package com.contrastsecurity.demo.providersearch;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProviderSearchDAO {
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    public List<Object[]> getProvidersInZipCode(String zipCode) {
        EntityManager em = entityManagerFactory.createEntityManager();
        String q = "select * from PROVIDERS where public_listing = true and zip_code = ?1";
        Query query = em.createNativeQuery(q);
        query.setParameter(1, zipCode);
        List<Object[]> results = query.getResultList();
        return results;
    }
}


