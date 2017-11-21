package com.vimeo.sample.model.scenarios;

import com.vimeo.stag.UseStag;

/**
 * This class represents the scenario where the model class is coded as an inner class where
 * the outer class is not annotated or otherwise known to the annotation processor.
 */
public class OuterClassWithInnerModel {

	@UseStag
	public static class InnerModel {

		public int version;

	}
}
