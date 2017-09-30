/*
* Copyright 2014 Michael Scharhag
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.mscharhag.oleaster.runner;

import com.mscharhag.oleaster.runner.suite.Spec;
import com.mscharhag.oleaster.runner.suite.Suite;
import com.mscharhag.oleaster.runner.suite.SuiteBuilder;
import com.mscharhag.oleaster.runner.suite.SuiteDefinition;
import com.mscharhag.oleaster.runner.suite.SuiteDefinitionEvaluator;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * OleasterRunner is JUnit runner that lets you write JUnit tests
 * like you write Jasmine tests (a popular Javascript testing framework).
 * <p>An Oleaster test looks like this:
 * <pre>
 * 	{@literal @}RunWith(OleasterRunner.class)
 * 	 public class RunnerIntroductionTest {{
 *		describe("A suite", () -&gt; {
 *			it("contains a spec with an expectation", () -&gt; {
 *				// test code
 *			});
 *		});
 *	}}
 * </pre>
 */
public class OleasterRunner extends ParentRunner<Spec> {

	public OleasterRunner(Class<?> testClass) throws InitializationError {
		super(testClass);
	}


	@Override
	protected List<Spec> getChildren() {
		SuiteBuilder suiteBuilder = this.createSuiteBuilder();
		SuiteDefinition baseSuiteDefinition = this.createBaseSuiteDefinition(suiteBuilder);
		SuiteDefinitionEvaluator evaluator = this.createSuiteDefinitionEvaluator();

		Suite suite = evaluator.evaluate(baseSuiteDefinition, suiteBuilder);

		return suite.collectSpecs();
	}


	@Override
	protected void runChild(Spec spec, RunNotifier notifier) {
		List<Spec> specs = spec.getSuite().getSpecs();
		boolean suiteHasNoSpecs = specs.isEmpty();
		boolean isFirstSpec = specs.indexOf(spec) == 0;
		boolean isLastSpec = specs.indexOf(spec) == specs.size() -1;

		if (suiteHasNoSpecs || isFirstSpec){
			runBeforeCallbacks(spec);
		}

		if (spec.getBlock().isPresent()) {
			runBeforeEachCallbacks(spec);
			runLeaf(spec, describeChild(spec), notifier);
			runAfterEachCallbacks(spec);
		} else {
			notifier.fireTestIgnored(describeChild(spec));
		}


		if (suiteHasNoSpecs || isLastSpec){
			runAfterCallbacks(spec);
		}
	}


	@Override
	public Description describeChild(Spec child) {
		return Description.createTestDescription(this.getTestClass().getJavaClass(), child.getFullDescription());
	}


	private SuiteBuilder createSuiteBuilder() {
		return new StaticSupportingSuiteBuilder();
	}


	private SuiteDefinition createBaseSuiteDefinition(SuiteBuilder suiteBuilder) {
		return new SuiteDefinition(null, null, () -> {
			Object obj = getTestClass().getJavaClass().newInstance();
			if (obj instanceof OleasterTest) {
				((OleasterTest) obj).buildTestSuite(suiteBuilder);
			}
		});
	}


	private SuiteDefinitionEvaluator createSuiteDefinitionEvaluator() {
		return new SuiteDefinitionEvaluator();
	}


	void runBeforeEachCallbacks(Spec spec) {
		this.runInvokables(this.collectInvokables(spec.getSuite(), Suite::getBeforeEachHandlers, true));
	}


	void runBeforeCallbacks(Spec spec) {
		this.runInvokables(this.collectInvokables(spec.getSuite(), Suite::getBeforeHandlers, true));
	}


	void runAfterEachCallbacks(Spec spec) {
		this.runInvokables(this.collectInvokables(spec.getSuite(), Suite::getAfterEachHandlers, false));
	}


	void runAfterCallbacks(Spec spec) {
		this.runInvokables(this.collectInvokables(spec.getSuite(), Suite::getAfterHandlers, false));
	}


	private List<Invokable> collectInvokables(
			Suite suite,
			Function<Suite, List<Invokable>> method,
			boolean reverseOrder) {
		List<List<Invokable>> lists = new ArrayList<>();
		Suite parent = suite;
		while (parent != null) {
			lists.add(new ArrayList<>(method.apply(parent)));
			parent = parent.getParent();
		}

		if (reverseOrder) {
			Collections.reverse(lists);
		}

		List<Invokable> flatList = new ArrayList<>();
		for (List<Invokable> list : lists) {
			flatList.addAll(list);
		}
		return flatList;
	}


	private void runInvokables(List<Invokable> invokables) {
		invokables.forEach(callback -> {
			try {
				callback.invoke();
			} catch (Exception e) {
				throw new RuntimeException("An exception occurred while running invokable: " + e.getMessage(), e);
			}
		});
	}
}
