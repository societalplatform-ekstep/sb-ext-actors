package com.infosys.controller;

import com.infosys.service.ContentAnalyticsService;
import com.infosys.util.ElasticUtil;
import com.infosys.util.LexProjectUtil;
import com.infosys.util.SearchConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.sunbird.common.Constants;
import org.sunbird.common.models.response.Response;
import org.sunbird.common.models.util.ProjectUtil;
import org.sunbird.common.responsecode.ResponseCode;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class ContentAnalyticsController {

	private final ContentAnalyticsService contentAnalyticsService;

	public ContentAnalyticsController(ContentAnalyticsService contentAnalyticsService) {
		this.contentAnalyticsService = contentAnalyticsService;
	}

	/**
	 * @param rootOrg       Name of root Organization
	 * @param org           Name of Organization inside rootOrg
	 * @param langCode      e.g. en,ar,es
	 * @param contentStatus Draft,Reviewed,Live
	 * @param startDate     Optional format = yyyy-MM-dd HH:mm:ss timeZone = GMT
	 * @param endDate       Optional format = yyyy-MM-dd HH:mm:ss timeZone = GMT
	 * @return Response model of  ount of contentType Resource,Collection,Course etc based on status, date filters
	 */
	@GetMapping("/v1/content/{langCode}/{contentStatus}/count")
	public ResponseEntity<Response> getContentCountStats(@RequestHeader("rootOrg") String rootOrg,
														 @RequestHeader("org") String org,
														 @PathVariable("langCode") String langCode,
														 @PathVariable("contentStatus") String contentStatus,
														 @RequestParam(value = "startDate", required = false) Timestamp startDate,
														 @RequestParam(value = "endDate", required = false) Timestamp endDate) {
		HttpStatus status;
		Response resp = new Response();
		resp.setVer("v1");
		resp.setId("api.getContentCountStats");
		resp.setTs(ProjectUtil.getFormattedDate());
		try {
			// Parsing in format required by ElasticSearch
			String sDate = LexProjectUtil.getEsFormattedDate(startDate);
			String eDate = LexProjectUtil.getEsFormattedDate(endDate);
			Map<String, Object> result = contentAnalyticsService.getContentCountByStatus(rootOrg, org, langCode, contentStatus, sDate, eDate);
			status = result.isEmpty() ? HttpStatus.NO_CONTENT : HttpStatus.OK;
			resp.put(Constants.RESPONSE, result);
		} catch (Exception e) {
			ResponseCode responseCode = ResponseCode.getResponse(ResponseCode.internalError.getErrorCode());
			resp.put("Error", e.getMessage());
			resp.setResponseCode(responseCode);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(resp, status);
	}

	/**
	 * @param rootOrg       Name of root Organization
	 * @param org           Name of Organization inside rootOrg
	 * @param langCode      e.g. en,ar,es
	 * @param searchSize    Search Size
	 * @param offSet        Defines the offset from the first result you want to fetch
	 * @param startDate     Optional format = yyyy-MM-dd HH:mm:ss timeZone = GMT
	 * @param endDate       Optional format = yyyy-MM-dd HH:mm:ss timeZone = GMT
	 * @param includeFields Optional comma seperated required fields default = identifier,name
	 * @param search_query   Optional search query based on which list will be displayed of external resources
	 * @return List of External Resources which are in Live state based on date filters if provided
	 */
	@GetMapping("/v1/content/{langCode}/external/resources")
	public ResponseEntity<?> getExternalResources(@RequestHeader("rootOrg") String rootOrg,
												  @RequestHeader("org") String org,
												  @PathVariable("langCode") String langCode,
												  @RequestParam(value = "searchSize", required = false, defaultValue = "1000") int searchSize,
												  @RequestParam(value = "offSet", required = false, defaultValue = "0") int offSet,
												  @RequestParam(value = "startDate", required = false) Timestamp startDate,
												  @RequestParam(value = "endDate", required = false) Timestamp endDate,
												  @RequestParam(value = "include", required = false, defaultValue = "identifier,name") String[] includeFields,
												  @RequestParam(value = "search_query",required = false , defaultValue = "*") String search_query) {
		HttpStatus status;
		Response resp = new Response();
		resp.setVer("v1");
		resp.setId("api.getExternalResources");
		resp.setTs(ProjectUtil.getFormattedDate());
		try {
			// Parsing in format required by ElasticSearch
			search_query = !search_query.equals("*") ? "*" + search_query + "*" : search_query;
			String sDate = LexProjectUtil.getEsFormattedDate(startDate);
			String eDate = LexProjectUtil.getEsFormattedDate(endDate);
			List<Map<String, Object>> result = contentAnalyticsService.getExternalResourcesList(rootOrg, org, langCode, sDate, eDate, searchSize, offSet, includeFields,search_query);
			status = result.isEmpty() ? HttpStatus.NO_CONTENT : HttpStatus.OK;
			resp.put(Constants.RESPONSE, result);
		} catch (Exception e) {
			ResponseCode responseCode = ResponseCode.getResponse(ResponseCode.internalError.getErrorCode());
			resp.put("Error", e.getMessage());
			resp.setResponseCode(responseCode);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(resp, status);
	}

	/**
	 * @param rootOrg       Name of root Organization
	 * @param org           Name of Organization inside rootOrg
	 * @param langCode      e.g. en,ar,es
	 * @param contentType   Course, Collection, Resource
	 * @param contentStatus Draft,Reviewed,Live
	 * @param searchSize    Search Size
	 * @param offSet        Defines the offset from the first result you want to fetch
	 * @param startDate     Optional format = yyyy-MM-dd HH:mm:ss timeZone = GMT
	 * @param endDate       Optional format = yyyy-MM-dd HH:mm:ss timeZone = GMT
	 * @param includeFields Optional comma seperated required fields default = identifier,name
	 * @param search_query  Optional search query based on which list will be displayed of external resources
	 * @return List of External Resources which are in Live state based on date filters if provided
	 */
	@GetMapping("/v1/content/{langCode}/{contentType}/{contentStatus}")
	public ResponseEntity<?> getFilteredContentList(@RequestHeader("rootOrg") String rootOrg,
													@RequestHeader("org") String org,
													@PathVariable("langCode") String langCode,
													@PathVariable("contentType") String contentType,
													@PathVariable("contentStatus") String contentStatus,
													@RequestParam(value = "searchSize", required = false, defaultValue = "1000") int searchSize,
													@RequestParam(value = "offSet", required = false, defaultValue = "0") int offSet,
													@RequestParam(value = "startDate", required = false) Timestamp startDate,
													@RequestParam(value = "endDate", required = false) Timestamp endDate,
													@RequestParam(value = "include", required = false, defaultValue = "identifier,name") String[] includeFields,
													@RequestParam(value = "search_query", required = false, defaultValue = "*") String search_query) {
		HttpStatus status;
		Response resp = new Response();
		resp.setVer("v1");
		resp.setId("api.getFilteredContentList");
		resp.setTs(ProjectUtil.getFormattedDate());
		try {
			// Parsing in format required by ElasticSearch
			search_query = !search_query.equals("*") ? "*" + search_query + "*" : search_query;
			String sDate = LexProjectUtil.getEsFormattedDate(startDate);
			String eDate = LexProjectUtil.getEsFormattedDate(endDate);
			Map<String, Map<String, Map<String, Object>>> searchFilters = ElasticUtil.getSearchFiltersDefaultMap();
			Map<String, Object> mustTermFilters = searchFilters.get(SearchConstants.MUST).get(SearchConstants.TERM);
			mustTermFilters.put(com.infosys.util.Constants.ML_SEARCH.STATUS, StringUtils.capitalize(contentStatus));
			mustTermFilters.put(com.infosys.util.Constants.ML_SEARCH.CONTENT_TYPE, StringUtils.capitalize(contentType));
			mustTermFilters.put(com.infosys.util.Constants.ML_SEARCH.ROOT_ORG, rootOrg);
			mustTermFilters.put(com.infosys.util.Constants.ML_SEARCH.ACCESS_PATHS, rootOrg + com.infosys.util.Constants.ML_SEARCH.ACCESS_PATHS_SEPARATOR + org);
			Map<String, Object> rangeFilters = searchFilters.get(SearchConstants.FILTERS).get(SearchConstants.RANGE);
			rangeFilters.put(contentStatus.equalsIgnoreCase(com.infosys.util.Constants.ML_SEARCH.STATUS_LIVE) ?
					com.infosys.util.Constants.ML_SEARCH.LAST_PUBLISHED_ON : com.infosys.util.Constants.ML_SEARCH.LAST_UPDATED_ON,
					new HashMap<Object, Object>(){{
						if (StringUtils.hasText(sDate)) {
							put(SearchConstants.GTE, sDate);
						}
						if (StringUtils.hasText(eDate)) {
							put(SearchConstants.LTE, eDate);
						}
					}});
			List<Map<String, Object>> result = contentAnalyticsService.getContentList(searchFilters, langCode, searchSize, offSet, includeFields, search_query);
			status = result.isEmpty() ? HttpStatus.NO_CONTENT : HttpStatus.OK;
			resp.put(Constants.RESPONSE, result);
		} catch (Exception e) {
			ResponseCode responseCode = ResponseCode.getResponse(ResponseCode.internalError.getErrorCode());
			resp.put("Error", e.getMessage());
			resp.setResponseCode(responseCode);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(resp, status);
	}
}
