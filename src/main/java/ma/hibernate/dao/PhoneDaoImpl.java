package ma.hibernate.dao;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.Map;
import ma.hibernate.model.Phone;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class PhoneDaoImpl extends AbstractDao implements PhoneDao {
    public PhoneDaoImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    public Phone create(Phone phone) {
        try (Session session = factory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.save(phone);
            transaction.commit();
        }
        return phone;
    }

    @Override
    public List<Phone> findAll(Map<String, String[]> params) {
        try (Session session = factory.openSession()) {
            Transaction transaction = session.beginTransaction();
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Phone> cq = cb.createQuery(Phone.class);
            Root<Phone> root = cq.from(Phone.class);

            List<Predicate> predicates = params.entrySet().stream()
                    .filter(entry -> entry.getValue() != null && entry.getValue().length > 0)
                    .map(entry -> {
                        Path<String> path = root.get(entry.getKey());
                        return entry.getValue().length == 1
                                ? cb.equal(path, entry.getValue()[0])
                                : path.in((Object[]) entry.getValue());
                    })
                    .toList();

            cq.select(root).where(predicates.toArray(new Predicate[0]));

            List<Phone> result = session.createQuery(cq).getResultList();
            transaction.commit();
            return result;
        }
    }
}
