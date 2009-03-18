package org.obiba.onyx.jade.instrument.cardiffuniversity;

/**
 * Noddle test codes and associated test name asset keys.
 * @author tdebat
 * 
 */
public enum NoddleTests {
  /** Reaction Time */
  RT(11, "reactionTime"),
  /** Paired Associates Learning */
  PA(21, "pairedAssociatesLearning"),
  /** Reasoning Quiz */
  RQ(31, "reasoningQuiz"),
  /** Attention Interface */
  ST(41, "attentionInterface"),
  /** Working Memory */
  WM(51, "workingMemory");

  private int testDataCode;

  private String assetKey;

  NoddleTests(int testDataCode, String assetKey) {
    this.testDataCode = testDataCode;
    this.assetKey = assetKey;
  }

  /** Used to mark test data in the result file. */
  public int getTestDataCode() {
    return testDataCode;
  }

  /** Used to mark the beginning of test data in the result file. */
  public int getStartDataCode() {
    return testDataCode - 1;
  }

  /** Used to mark the end of test data in the result file. */
  public int getEndDataCode() {
    return testDataCode + 1;
  }

  public String getAssetKey() {
    return assetKey;
  }

}
