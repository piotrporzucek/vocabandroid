package pl.egalit.vocab.learn.words;

public enum WordAnswerEnum {
	WELL(7), UNSURE(3), NOIDEA(1);

	private int daysToRepeat;

	private WordAnswerEnum(int daysToRepeat) {
		this.daysToRepeat = daysToRepeat;
	}

	public static int getDaysForRepeat(int state) {
		for (WordAnswerEnum answerEnum : values()) {
			if (answerEnum.ordinal() == state) {
				return answerEnum.getDaysToRepeat();
			}
		}
		return -1;
	}

	public int getDaysToRepeat() {
		return daysToRepeat;
	}

}
