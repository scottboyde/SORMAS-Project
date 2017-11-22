package de.symeda.sormas.backend.sample;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotNull;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.caze.CaseReferenceDto;
import de.symeda.sormas.api.region.DistrictReferenceDto;
import de.symeda.sormas.api.sample.DashboardSample;
import de.symeda.sormas.api.sample.SampleDto;
import de.symeda.sormas.api.sample.SampleFacade;
import de.symeda.sormas.api.sample.SampleIndexDto;
import de.symeda.sormas.api.sample.SampleReferenceDto;
import de.symeda.sormas.api.sample.SampleTestDto;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.backend.caze.Case;
import de.symeda.sormas.backend.caze.CaseFacadeEjb;
import de.symeda.sormas.backend.caze.CaseFacadeEjb.CaseFacadeEjbLocal;
import de.symeda.sormas.backend.caze.CaseService;
import de.symeda.sormas.backend.facility.Facility;
import de.symeda.sormas.backend.facility.FacilityFacadeEjb;
import de.symeda.sormas.backend.facility.FacilityFacadeEjb.FacilityFacadeEjbLocal;
import de.symeda.sormas.backend.facility.FacilityService;
import de.symeda.sormas.backend.region.District;
import de.symeda.sormas.backend.region.DistrictFacadeEjb.DistrictFacadeEjbLocal;
import de.symeda.sormas.backend.region.DistrictService;
import de.symeda.sormas.backend.region.Region;
import de.symeda.sormas.backend.region.RegionFacadeEjb.RegionFacadeEjbLocal;
import de.symeda.sormas.backend.sample.SampleTestFacadeEjb.SampleTestFacadeEjbLocal;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.user.UserFacadeEjb;
import de.symeda.sormas.backend.user.UserService;
import de.symeda.sormas.backend.util.DtoHelper;
import de.symeda.sormas.backend.util.ModelConstants;

@Stateless(name = "SampleFacade")
public class SampleFacadeEjb implements SampleFacade {

	@PersistenceContext(unitName = ModelConstants.PERSISTENCE_UNIT_NAME)
	protected EntityManager em;
	
	@EJB
	private SampleService sampleService;
	@EJB
	private SampleTestService sampleTestService;
	@EJB
	private UserService userService;
	@EJB
	private CaseService caseService;
	@EJB
	private DistrictService districtService;
	@EJB
	private FacilityService facilityService;
	@EJB
	private CaseFacadeEjbLocal caseFacade;
	@EJB
	private RegionFacadeEjbLocal regionFacade;
	@EJB
	private DistrictFacadeEjbLocal districtFacade;
	@EJB
	private FacilityFacadeEjbLocal facilityFacade;
	@EJB
	private SampleTestFacadeEjbLocal sampleTestFacade;

	@Override
	public List<String> getAllUuids(String userUuid) {
		
		User user = userService.getByUuid(userUuid);
		
		if (user == null) {
			return Collections.emptyList();
		}
		
		return sampleService.getAllUuids(user);
	}	
	
	@Override
	public List<SampleDto> getAllAfter(Date date, String userUuid) {
		User user = userService.getByUuid(userUuid);
		
		if(user == null) {
			return Collections.emptyList();
		}
		
		return sampleService.getAllAfter(date, user).stream()
				.map(e -> toDto(e))
				.collect(Collectors.toList());
	}
	
	@Override
	public List<SampleDto> getByUuids(List<String> uuids) {
		return sampleService.getByUuids(uuids)
				.stream()
				.map(c -> toDto(c))
				.collect(Collectors.toList());
	}

	@Override
	public List<SampleDto> getAllByCase(CaseReferenceDto caseRef) {
		if(caseRef == null) {
			return Collections.emptyList();
		}
		
		Case caze = caseService.getByUuid(caseRef.getUuid());
		
		return sampleService.getAllByCase(caze).stream()
				.map(s -> toDto(s))
				.collect(Collectors.toList());
	}

	@Override
	public SampleDto getSampleByUuid(String uuid) {
		return toDto(sampleService.getByUuid(uuid));
	}

	@Override
	public SampleDto saveSample(SampleDto dto) {
		Sample sample = fromDto(dto);
		sampleService.ensurePersisted(sample);
		
		return toDto(sample);
	}
	
	@Override
	public SampleReferenceDto getReferenceByUuid(String uuid) {
		return toReferenceDto(sampleService.getByUuid(uuid));
	}
	
	@Override
	public List<SampleIndexDto> getIndexList(String userUuid, CaseReferenceDto caseRef) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<SampleIndexDto> cq = cb.createQuery(SampleIndexDto.class);
		Root<Sample> sample = cq.from(Sample.class);
		Join<Sample, Sample> sampleJoin = sample.join(Sample.REFERRED_TO, JoinType.LEFT);
		Join<Sample, Facility> lab = sample.join(Sample.LAB, JoinType.LEFT);
		Join<Sample, Case> caze = sample.join(Sample.ASSOCIATED_CASE, JoinType.LEFT);
		Join<Case, Region> caseRegion = caze.join(Case.REGION, JoinType.LEFT);
		Join<Case, District> caseDistrict = caze.join(Case.DISTRICT, JoinType.LEFT);
		
		cq.multiselect(sample.get(Sample.UUID), caze.get(Case.UUID), sample.get(Sample.SAMPLE_CODE), sample.get(Sample.LAB_SAMPLE_ID),
				caze.get(Case.DISEASE), caze.get(Case.DISEASE_DETAILS), caseRegion.get(Region.UUID), 
				caseDistrict.get(District.UUID), sample.get(Sample.SHIPPED), sample.get(Sample.RECEIVED),
				sampleJoin.get(Sample.UUID), sample.get(Sample.SHIPMENT_DATE), sample.get(Sample.RECEIVED_DATE), 
				lab.get(Facility.UUID), sample.get(Sample.SAMPLE_MATERIAL), sample.get(Sample.SPECIMEN_CONDITION));
		
		Predicate filter = null;
		if (userUuid != null) {
			User user = userService.getByUuid(userUuid);
			filter = sampleService.createUserFilter(cb, cq, sample, user);
		}
			
		if (caseRef != null) {
			Case sampleCase = caseService.getByReferenceDto(caseRef);
			Predicate caseFilter = cb.equal(sample.get(Sample.ASSOCIATED_CASE), sampleCase);
			if (filter != null) {
				filter = cb.and(filter, caseFilter);
			} else {
				filter = caseFilter;
			}
		}
		
		if (filter != null) {
			cq.where(filter);
		}
		
		List<SampleIndexDto> resultList = em.createQuery(cq).getResultList();
		for (SampleIndexDto indexDto : resultList) {
			indexDto.setAssociatedCase(caseFacade.getReferenceByUuid(indexDto.getAssociatedCaseUuid()));
			indexDto.setCaseRegion(regionFacade.getRegionReferenceByUuid(indexDto.getCaseRegionUuid()));
			indexDto.setCaseDistrict(districtFacade.getDistrictReferenceByUuid(indexDto.getCaseDistrictUuid()));
			indexDto.setReferred(getReferenceByUuid(indexDto.getReferredSampleUuid()) != null);
			indexDto.setLab(facilityFacade.getFacilityReferenceByUuid(indexDto.getLabUuid()));
			
			SampleTestDto latestSampleTest = null;
			for(SampleTestDto sampleTest : sampleTestFacade.getAllBySample(indexDto)) {
				if(latestSampleTest == null) {
					latestSampleTest = sampleTest;
				} else {
					if(sampleTest.getTestDateTime().after(latestSampleTest.getTestDateTime())) {
						latestSampleTest = sampleTest;
					}
				}
			}
			
			if(latestSampleTest != null) {
				indexDto.setLabUser(latestSampleTest.getLabUser());
				indexDto.setTestResult(latestSampleTest.getTestResult());
			}
		}
		
		return resultList;	
	}
	
	@Override
	public List<DashboardSample> getNewSamplesForDashboard(DistrictReferenceDto districtRef, Disease disease, Date from, Date to, String userUuid) {
		User user = userService.getByUuid(userUuid);
		District district = districtService.getByReferenceDto(districtRef);
		
		return sampleService.getNewSamplesForDashboard(district, disease, from, to, user);
	}
	
	@Override
	public SampleReferenceDto getReferredFrom(String sampleUuid) {
		return toReferenceDto(sampleService.getReferredFrom(sampleUuid));
	}
	
	@Override
	public void deleteSample(SampleReferenceDto sampleRef, String userUuid) {
		User user = userService.getByUuid(userUuid);
		if (!user.getUserRoles().contains(UserRole.ADMIN)) {
			throw new UnsupportedOperationException("Only admins are allowed to delete entities.");
		}

		Sample sample = sampleService.getByReferenceDto(sampleRef);
		List<SampleTest> sampleTests = sampleTestService.getAllBySample(sample);
		for (SampleTest sampleTest : sampleTests) {
			sampleTestService.delete(sampleTest);
		}
		sampleService.delete(sample);
	}
	
	public Sample fromDto(@NotNull SampleDto source) {
		
		Sample target = sampleService.getByUuid(source.getUuid());
		if(target == null) {
			target = new Sample();
			target.setUuid(source.getUuid());
			if(source.getCreationDate() != null) {
				target.setCreationDate(new Timestamp(source.getCreationDate().getTime()));
			}
		}
		DtoHelper.validateDto(source, target);
		
		target.setAssociatedCase(caseService.getByReferenceDto(source.getAssociatedCase()));
		target.setSampleCode(source.getSampleCode());
		target.setLabSampleID(source.getLabSampleID());
		target.setSampleDateTime(source.getSampleDateTime());
		target.setReportDateTime(source.getReportDateTime());
		target.setReportingUser(userService.getByReferenceDto(source.getReportingUser()));
		target.setSampleMaterial(source.getSampleMaterial());
		target.setSampleMaterialText(source.getSampleMaterialText());
		target.setLab(facilityService.getByReferenceDto(source.getLab()));
		target.setShipmentDate(source.getShipmentDate());
		target.setShipmentDetails(source.getShipmentDetails());
		target.setReceivedDate(source.getReceivedDate());
		target.setSpecimenCondition(source.getSpecimenCondition());
		target.setNoTestPossibleReason(source.getNoTestPossibleReason());
		target.setComment(source.getComment());
		target.setSampleSource(source.getSampleSource());
		target.setSuggestedTypeOfTest(source.getSuggestedTypeOfTest());
		target.setReferredTo(sampleService.getByReferenceDto(source.getReferredTo()));
		target.setShipped(source.isShipped());
		target.setReceived(source.isReceived());

		target.setReportLat(source.getReportLat());
		target.setReportLon(source.getReportLon());
		target.setReportLatLonAccuracy(source.getReportLatLonAccuracy());
		
		return target;
	}
	
	public static SampleDto toDto(Sample source) {
		if(source == null) {
			return null;
		}
		SampleDto target = new SampleDto();
		DtoHelper.fillReferenceDto(target, source);
		
		target.setAssociatedCase(CaseFacadeEjb.toReferenceDto(source.getAssociatedCase()));
		target.setSampleCode(source.getSampleCode());
		target.setLabSampleID(source.getLabSampleID());
		target.setSampleDateTime(source.getSampleDateTime());
		target.setReportDateTime(source.getReportDateTime());
		target.setReportingUser(UserFacadeEjb.toReferenceDto(source.getReportingUser()));
		target.setSampleMaterial(source.getSampleMaterial());
		target.setSampleMaterialText(source.getSampleMaterialText());
		target.setLab(FacilityFacadeEjb.toReferenceDto(source.getLab()));
		target.setShipmentDate(source.getShipmentDate());
		target.setShipmentDetails(source.getShipmentDetails());
		target.setReceivedDate(source.getReceivedDate());
		target.setSpecimenCondition(source.getSpecimenCondition());
		target.setNoTestPossibleReason(source.getNoTestPossibleReason());
		target.setComment(source.getComment());
		target.setSampleSource(source.getSampleSource());
		target.setSuggestedTypeOfTest(source.getSuggestedTypeOfTest());
		target.setReferredTo(SampleFacadeEjb.toReferenceDto(source.getReferredTo()));
		target.setShipped(source.isShipped());
		target.setReceived(source.isReceived());

		target.setReportLat(source.getReportLat());
		target.setReportLon(source.getReportLon());
		target.setReportLatLonAccuracy(source.getReportLatLonAccuracy());
		
		return target;
	}
	
	public static SampleReferenceDto toReferenceDto(Sample entity) {
		if(entity == null) {
			return null;
		}
		SampleReferenceDto dto = new SampleReferenceDto();
		DtoHelper.fillReferenceDto(dto, entity);
		return dto;
	}
	
}
