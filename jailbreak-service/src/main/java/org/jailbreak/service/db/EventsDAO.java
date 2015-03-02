package org.jailbreak.service.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.jailbreak.api.representations.Representations.Event;
import org.jailbreak.api.representations.Representations.Event.EventsFilters;
import org.jailbreak.service.db.SimplestSqlBuilder.OrderBy;
import org.jailbreak.service.db.mappers.EventsMapper;
import org.jailbreak.service.db.mappers.RowCountMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

@RegisterMapper(EventsMapper.class)
public abstract class EventsDAO {
	
	public Connection conn;
	private final Logger LOG = LoggerFactory.getLogger(EventsDAO.class);
	
	@SqlQuery("SELECT * FROM events WHERE id = :id")
	@SingleValueResult(Event.class)
	public abstract Optional<Event> getEvent(@Bind("id") int id);
	
	@SqlQuery("SELECT * FROM events ORDER BY time DESC LIMIT :limit")
	public abstract List<Event> getEvents(@Bind("limit") int limit);
	
	public List<Event> getFilteredEvents(int limit, EventsFilters filters) throws SQLException {
		// Build query and bind in params
		Map<String, Object> bindParams = Maps.newHashMap();
		SimplestSqlBuilder builder = applyWhereFilters(filters, bindParams);
		builder.addOrderBy("time", OrderBy.DESC);
		builder.limit(limit);
		String queryString = builder.build();
		
		LOG.debug("getFilteredEvents SQL: " + queryString);
		
		ManualStatement query = new ManualStatement(conn, queryString, bindParams);
		List<Event> results = query.executeQuery(new EventsMapper());
		return results;
	}
	
	public int countFilteredEvents(EventsFilters filters) throws SQLException {
		// Build query and bind in params
		Map<String, Object> bindParams = Maps.newHashMap();
		SimplestSqlBuilder builder = applyWhereFilters(filters, bindParams);
		builder.addColumn("COUNT(*) as count");
		String queryString = builder.build();
		
		LOG.debug("countFilteredEvents SQL: " + queryString);
		
		ManualStatement query = new ManualStatement(conn, queryString, bindParams);
		Integer count = query.executeQuery(new RowCountMapper()).get(0);
		return count;
	}
	
	private SimplestSqlBuilder applyWhereFilters(EventsFilters filters, Map<String, Object> bindParams) {
		SimplestSqlBuilder builder = new SimplestSqlBuilder("events");
		
		if (filters.hasTeamId()) {
			builder.addWhere("team_id = :team_id");
			bindParams.put("team_id", filters.getTeamId());
		}
		if (filters.hasType()) {
			builder.addWhere("type = :type");
			bindParams.put("type", filters.getType().ordinal());
		}
		if (filters.hasBeforeId()) {
			builder.addWhere("id < :beforeId");
			bindParams.put("before_id", filters.getBeforeId());
		}
		if (filters.hasAfterId()) {
			builder.addWhere("id > :after_id");
			bindParams.put("after_id", filters.getAfterId());
		}
		
		return builder;
	}
	
}