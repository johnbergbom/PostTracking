package fi.lauber.posttracking.thread;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component(value = "hibernateSessionAspect")
@Aspect
public class BackendRunnerAspect {
	
	public static final Logger logger = Logger.getLogger(BackendRunnerAspect.class);
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Around("execution(* fi.lauber.posttracking.thread.BackendLogic.updateTrackingStatuses(..))")
	public Object executeInHibernateSession(ProceedingJoinPoint pjp) throws Throwable {
		try {
			logger.debug("executeInHibernateSession.before");
			logger.debug("executeInHibernateSession.before.sessionFactory = " + sessionFactory);
			Session session = SessionFactoryUtils.getSession(sessionFactory, true);
			session.setFlushMode(FlushMode.AUTO);
			TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
			logger.debug("Hibernate session opened.");
			return pjp.proceed();
		} catch (Throwable e) {
			logger.debug("Error detected in executeInHibernateSession: ", e);
			throw e;
		} finally {
			logger.debug("Closing hibernate session");
			SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.unbindResource(sessionFactory);
			Session session = sessionHolder.getSession();
			session.flush();
			SessionFactoryUtils.closeSession(session);
			logger.debug("Hibernate session closed");
		}
	}

}
