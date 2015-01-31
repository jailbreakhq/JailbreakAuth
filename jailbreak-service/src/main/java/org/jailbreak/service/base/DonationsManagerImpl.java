package org.jailbreak.service.base;

import java.util.List;

import org.jailbreak.api.representations.Representations.Donation;
import org.jailbreak.api.representations.Representations.Donation.DonationsFilters;
import org.jailbreak.service.core.DonationsManager;
import org.jailbreak.service.db.DonationsDAO;

import com.google.common.base.Optional;
import com.google.inject.Inject;

public class DonationsManagerImpl implements DonationsManager {
	
	private final DonationsDAO dao;
	
	@Inject
	public DonationsManagerImpl(DonationsDAO dao) {
		this.dao = dao;
	}

	@Override
	public Optional<Donation> getDonation(int id) {
		return this.dao.getDonation(id);
	}

	@Override
	public boolean updateDonation(Donation donation) {
		int result = this.dao.update(donation);
		if (result == 1)
			return true;
		else
			return false;
	}
	
	@Override
	public Donation createDonation(Donation donation) {
		int newId = this.dao.insert(donation);
		
		return this.dao.getDonation(newId).get(); // return full object with defaults set by DB
	}
	
	@Override
	public Optional<Donation> patchDonation(Donation donation) {
		Optional<Donation> maybeCurrent = dao.getDonation(donation.getId());
		if (maybeCurrent.isPresent()) {
			Donation merged = maybeCurrent.get().toBuilder().mergeFrom(donation).build();
			dao.update(merged);
			return Optional.of(merged);
		}
		return Optional.absent();
	}

	@Override
	public List<Donation> getDonations() {
		return this.dao.getDonations();
	}
	
	@Override
	public List<Donation> getDonations(DonationsFilters filters) {
		return this.dao.getFilteredDonations(filters);
	}

	@Override
	public boolean deleteDonation(int id) {
		int result = this.dao.delete(id);
		if (result == 1)
			return true;
		else
			return false;
	}

}