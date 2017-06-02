package com.fortify.processrunner.ssc.processor.composite;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fortify.processrunner.common.issue.IssueState;
import com.fortify.processrunner.common.processor.AbstractProcessorUpdateIssueStateForVulnerabilities;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.processrunner.processor.AbstractCompositeProcessor;
import com.fortify.processrunner.processor.IProcessor;
import com.fortify.processrunner.ssc.connection.SSCConnectionFactory;
import com.fortify.processrunner.ssc.processor.enrich.SSCProcessorEnrichWithBugDataFromCustomTag;
import com.fortify.processrunner.ssc.processor.enrich.SSCProcessorEnrichWithIssueDetails;
import com.fortify.processrunner.ssc.processor.enrich.SSCProcessorEnrichWithVulnDeepLink;
import com.fortify.processrunner.ssc.processor.enrich.SSCProcessorEnrichWithVulnState;
import com.fortify.processrunner.ssc.processor.filter.SSCFilterOnTopLevelField;
import com.fortify.processrunner.ssc.processor.retrieve.SSCProcessorRetrieveVulnerabilities;
import com.fortify.util.spring.SpringExpressionUtil;

/**
 * This {@link IProcessor} implementation allows for updating tracker state
 * based on SSC vulnerability state. It will retrieve all SSC vulnerabilities
 * (both open and closed) that have been previously submitted to the bug tracker,
 * group them by external bug link/id, and determine whether all vulnerabilities
 * in each group can be considered 'closed' and thus the corresponding bug
 * can be closed as well. 
 * 
 * TODO Test this SSC implementation for correct operation
 */
@Component
public class SSCProcessorUpdateBugTrackerState extends AbstractCompositeProcessor {
	private final SSCProcessorEnrichWithVulnState enrichWithVulnStateProcessor = new SSCProcessorEnrichWithVulnState(); 
	private String customTagName = "BugLink";
	
	private AbstractProcessorUpdateIssueStateForVulnerabilities<?> updateIssueStateProcessor;
	
	@Override
	protected void addCompositeContextProperties(Collection<ContextProperty> contextProperties, Context context) {
		SSCConnectionFactory.addContextProperties(contextProperties, context);
	}
	
	@Override
	public List<IProcessor> getProcessors() {
		return Arrays.asList(createRootVulnerabilityArrayProcessor());
	}
	
	protected IProcessor createRootVulnerabilityArrayProcessor() {
		SSCProcessorRetrieveVulnerabilities result = new SSCProcessorRetrieveVulnerabilities(
			new SSCFilterOnTopLevelField(getCustomTagName(), "<none>", true),
			new SSCProcessorEnrichWithIssueDetails(),
			new SSCProcessorEnrichWithVulnDeepLink(),
			new SSCProcessorEnrichWithBugDataFromCustomTag(getCustomTagName()),
			getVulnState(),
			getUpdateIssueStateProcessor()
		);
		result.getIssueSearchOptions().setIncludeHidden(false);
		result.getIssueSearchOptions().setIncludeRemoved(true);
		result.getIssueSearchOptions().setIncludeSuppressed(true);
		return result;
	}
	
	public SSCProcessorEnrichWithVulnState getVulnState() {
		return enrichWithVulnStateProcessor;
	}

	public AbstractProcessorUpdateIssueStateForVulnerabilities<?> getUpdateIssueStateProcessor() {
		return updateIssueStateProcessor;
	}

	public String getCustomTagName() {
		return customTagName;
	}

	public void setCustomTagName(String customTagName) {
		this.customTagName = customTagName;
	}

	@Autowired(required=false) // non-required to avoid Spring autowiring failures if bug tracker implementation doesn't include bug state management
	public void setUpdateIssueStateProcessor(AbstractProcessorUpdateIssueStateForVulnerabilities<?> updateIssueStateProcessor) {
		updateIssueStateProcessor.setGroupTemplateExpression(SpringExpressionUtil.parseTemplateExpression("${bugLink}"));
		updateIssueStateProcessor.setForceGrouping(true);
		updateIssueStateProcessor.setIsVulnStateOpenExpression(SpringExpressionUtil.parseSimpleExpression(SSCProcessorEnrichWithVulnState.NAME_VULN_STATE+"=='"+IssueState.OPEN.name()+"'"));
		updateIssueStateProcessor.setVulnBugLinkExpression(SpringExpressionUtil.parseSimpleExpression("bugLink"));
		this.updateIssueStateProcessor = updateIssueStateProcessor;
	}
	
	@Autowired(required=false)
	public void setConfiguration(SSCBugTrackerProcessorConfiguration config) {
		setCustomTagName(config.getCustomTagName());
		getVulnState().setIsVulnerabilityOpenExpression(config.getIsVulnerabilityOpenExpression());
	}
}