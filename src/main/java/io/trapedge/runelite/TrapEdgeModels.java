package io.trapedge.runelite;

import java.util.List;

class TrapEdgeSnapshot
{
	String generatedAt;
	String sourceGeneratedAt;
	String deliveryMode;
	String sourceLabel;
	String sourceBaseUrl;
	String helpText;
	List<TriageRow> triage;
	List<ActionRow> actions;
	List<MemoryRow> memoryPressure;
	List<MemoryPattern> memoryPatterns;
	List<PostmortemEntry> postmortemFeed;
	List<ProofCase> proofCases;
	ReplaySummary replaySummary;
	ReplaySummary assessmentSummary;
	int runtimePostmortemCount;
}

class TriageRow
{
	int itemId;
	String itemName;
	String executionConfidence;
	String executionConfidenceLabel;
	String recommendedAction;
	String recommendedActionLabel;
	String caseCategory;
	String reasonSummary;
	long usableProfitEstimate;
	int trapScore;
}

class ActionRow
{
	int itemId;
	String itemName;
	String currentState;
	String currentStateLabel;
	String recommendedAction;
	String actionLabel;
	String caseCategory;
	String sizeHint;
	String whyNow;
	List<String> matchedRules;
}

class MemoryRow
{
	int itemId;
	String itemName;
	String memoryRiskLevel;
	int previousMistakes;
	List<String> dominantPatterns;
	List<String> nextRuleHints;
}

class MemoryPattern
{
	String patternKey;
	String patternLabel;
	int timesSeen;
	String consequenceLabel;
	String nextRule;
}

class ReplaySummary
{
	int total;
	int exactPasses;
	int confidencePasses;
	int actionPasses;
	int categoryPasses;
	int flagCoveragePasses;
}

class ProofCase
{
	int itemId;
	String itemName;
	String executionConfidence;
	String executionConfidenceLabel;
	int trapScore;
	String recommendedAction;
	String recommendedActionLabel;
	String caseCategory;
	String reasonSummary;
	List<String> trapFlags;
	List<String> memoryNotes;
	long usableProfitEstimate;
	long fiveMinuteFlow;
	long oneHourFlow;
	RawAssessment raw;
}

class ItemDetail extends ProofCase
{
	String deliveryMode;
	String sourceLabel;
	String sourceBaseUrl;
	List<FlagDetail> flagDetails;
	List<StrengthDetail> strengths;
	List<String> matchedRules;
	ProofCase proofCase;
	ItemDetail liveAssessment;
	List<PostmortemEntry> runtimePostmortems;
}

class FlagDetail
{
	String trapType;
	String severity;
	String explanation;
	String evidenceBasis;
	String safeAlternative;
}

class StrengthDetail
{
	String strengthType;
	String explanation;
	String evidenceBasis;
}

class RawAssessment
{
	int itemId;
	String itemName;
	long buyPrice;
	long sellPrice;
	long apparentMargin;
	long taxAdjustedMargin;
	int buyLimit;
	long freshnessSeconds;
}

class PostmortemEntry
{
	String entryId;
	String source;
	int itemId;
	String itemName;
	String enteredAt;
	String exitedAt;
	long expectedMargin;
	long realizedMargin;
	String postmortemLabel;
	String notes;
	String nextRule;
	String createdAt;
	String whatWentWrong;
}
