package org.jailbreak.service.base;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.jailbreak.api.representations.Representations.Checkin;
import org.jailbreak.api.representations.Representations.Event;
import org.jailbreak.api.representations.Representations.Team;
import org.jailbreak.api.representations.Representations.Event.EventType;
import org.jailbreak.service.core.CheckinsManager;
import org.jailbreak.service.core.TeamsManager;
import org.jailbreak.service.core.events.EventsManager;
import org.jailbreak.service.db.dao.CheckinsDAO;
import org.jailbreak.service.errors.ApiDocs;
import org.jailbreak.service.errors.BadRequestException;
import org.jailbreak.service.helpers.DistanceHelper;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

public class CheckinsManagerImpl implements CheckinsManager {

	private final CheckinsDAO dao;
	private final TeamsManager teamsManager;
	private final EventsManager eventsManager;
	private final DistanceHelper distanceHelper;
	
	@Inject
	public CheckinsManagerImpl(CheckinsDAO dao,
			TeamsManager teamsManager,
			EventsManager eventsManager,
			DistanceHelper distanceHelper) {
		this.dao = dao;
		this.teamsManager = teamsManager;
		this.eventsManager = eventsManager;
		this.distanceHelper = distanceHelper;
	}
	
	@Override
	public Checkin createCheckin(Checkin checkin) {
		Optional<Team> team = teamsManager.getRawTeam(checkin.getTeamId());
		if (team.isPresent()) {
			int newId = this.dao.insert(checkin);
			
			// update teams last checkin id
			teamsManager.updateTeam(team.get().toBuilder().setLastCheckinId(newId).build());
			
			// update all team positions
			int thisTeamsNewPosition = teamsManager.updateAllTeamPositions(checkin.getTeamId());
			
			// add a note to the 
			boolean highlight = false;
			if (thisTeamsNewPosition < 3) {
				highlight = true;
			}
			
			Event event = Event.newBuilder()
					 .setType(EventType.CHECKIN)
					 .setObjectId(newId)
					 .setTeamId(checkin.getTeamId())
					 .setHighlight(highlight)
					 .build();
			 
			 eventsManager.createEvent(event);
			
			return this.addDistanceToX(this.getCheckin(newId).get());
		} else {
			throw new BadRequestException("There is no team that matches team id " + checkin.getTeamId(), ApiDocs.CHECKINS);
		}
	}
	
	@Override
	public Optional<Checkin> updateCheckin(Checkin checkin) {
		int result = dao.update(checkin);
		if (result == 0) {
			return Optional.of(checkin);
		} else {
			return Optional.absent();
		}
	}
	
	@Override
	public Optional<Checkin> getCheckin(int id) {
		return this.addDistanceToX(this.dao.getCheckin(id));
	}
	
	@Override
	public HashMap<Integer, Checkin> getCheckins(Set<Integer> ids) {
		if (ids.isEmpty()) {
			return Maps.newHashMap();
		}
		
		List<Checkin> checkins = this.addDistanceToX(this.dao.getCheckins(ids));
		
		HashMap<Integer, Checkin> map = Maps.newHashMap();
		for (Checkin checkin : checkins) {
			map.put(checkin.getId(), checkin);
		}
		
		return map;
	}
	
	@Override
	public Optional<Checkin> getTeamCheckin(int teamId, int id) {
		return this.addDistanceToX(this.dao.getTeamCheckin(teamId, id));
	}
	
	@Override
	public Optional<Checkin> getLastTeamCheckin(int teamId) {
		return this.addDistanceToX(this.dao.getLastTeamCheckin(teamId));
	}

	@Override
	public List<Checkin> getTeamCheckins(int teamId) {
		return this.addDistanceToX(this.dao.getTeamCheckins(teamId));
	}
	
	private Optional<Checkin> addDistanceToX(Optional<Checkin> maybeCheckin) {
		if (maybeCheckin.isPresent()) {
			return Optional.of(this.addDistanceToX(maybeCheckin.get()));
		}
		
		return Optional.absent();
	}
	
	private Checkin addDistanceToX(Checkin checkin) {
		double distance = distanceHelper.distanceToX(checkin);
		return checkin.toBuilder().setDistanceToX(distance).build();
	}
	
	private List<Checkin> addDistanceToX(List<Checkin> checkins) {
		List<Checkin> newCheckins = Lists.newArrayListWithCapacity(checkins.size());
		for (Checkin checkin : checkins) {
			Checkin newCheckin = this.addDistanceToX(checkin);
			newCheckins.add(newCheckin);
		}
		return newCheckins;
	}
	
}
