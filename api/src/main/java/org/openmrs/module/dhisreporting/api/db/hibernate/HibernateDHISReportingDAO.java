/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.dhisreporting.api.db.hibernate;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.dhisreporting.MappedIndicatorReport;
import org.openmrs.module.dhisreporting.api.db.DHISReportingDAO;
import org.springframework.util.ReflectionUtils;

/**
 * It is a default implementation of {@link DHISReportingDAO}.
 */
public class HibernateDHISReportingDAO implements DHISReportingDAO {
	protected final Log log = LogFactory.getLog(this.getClass());
	private DbSessionFactory sessionFactory;

	/**
	 * @param sessionFactory
	 *            the sessionFactory to set
	 */
	public void setSessionFactory(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * @return the sessionFactory
	 */
	public DbSessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public Connection getConnection() {
		try {
			// reflective lookup to bridge between Hibernate 3.x and 4.x
			Method connectionMethod = sessionFactory.getCurrentSession().getClass().getMethod("connection");
			return (Connection) ReflectionUtils.invokeMethod(connectionMethod, sessionFactory.getCurrentSession());
		} catch (NoSuchMethodException ex) {
			throw new IllegalStateException("Cannot find connection() method on Hibernate session", ex);
		}
	}

	@Override
	public void saveMappedIndicatorReport(MappedIndicatorReport mappedIndicatorReport) {
		sessionFactory.getCurrentSession().save(mappedIndicatorReport);
	}

	@Override
	public void deleteMappedIndicatorReport(MappedIndicatorReport mappedIndicatorReport) {
		sessionFactory.getCurrentSession().delete(mappedIndicatorReport);
	}

	@Override
	public MappedIndicatorReport getMappedIndicatorReport(Integer id) {
		return (MappedIndicatorReport) sessionFactory.getCurrentSession().get(MappedIndicatorReport.class, id);
	}

	@Override
	public MappedIndicatorReport getMappedIndicatorReportByUuid(String uuid) {
		MappedIndicatorReport mappedIndicatorReport = (MappedIndicatorReport) sessionFactory.getCurrentSession()
				.createQuery("from MappedIndicatorReport r where r.uuid = :uuid").setParameter("uuid", uuid)
				.uniqueResult();

		return mappedIndicatorReport;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MappedIndicatorReport> getAllMappedIndicatorReports() {
		return sessionFactory.getCurrentSession().createCriteria(MappedIndicatorReport.class).list();
	}
}