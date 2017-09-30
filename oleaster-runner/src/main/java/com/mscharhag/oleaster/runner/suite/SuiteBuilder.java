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
package com.mscharhag.oleaster.runner.suite;


import com.mscharhag.oleaster.runner.Invokable;
import com.mscharhag.oleaster.runner.PendingInvokable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SuiteBuilder {

	private Map<String, Invokable> suiteDefinitions;
	private Map<String, Invokable> focusedSuiteDefinitions;
	private Map<String, Optional<Invokable>> specDefinitions;
	private Map<String, Optional<Invokable>> focusedSpecDefinitions;
	private List<Invokable> beforeEachHandlers;
	private List<Invokable> beforeHandlers;
	private List<Invokable> afterEachHandlers;
	private List<Invokable> afterHandlers;

	public SuiteBuilder() {
		this.prepare();
	}

	private void prepare() {
		this.suiteDefinitions = new LinkedHashMap<>();
		this.focusedSuiteDefinitions = new LinkedHashMap<>();
		this.specDefinitions = new LinkedHashMap<>();
		this.focusedSpecDefinitions = new LinkedHashMap<>();
		this.beforeHandlers = new ArrayList<>();
		this.afterHandlers = new ArrayList<>();
		this.beforeEachHandlers = new ArrayList<>();
		this.afterEachHandlers = new ArrayList<>();
	}

	public void beforeEvaluation() {
		this.prepare();
	}

	public void afterEvaluation() {

	}

	public void describe(String description, Invokable definition) {
		throwExceptionWhenSuiteDescriptionExists(description);
		this.suiteDefinitions.put(description, definition);
	}

	public void fdescribe(String description, Invokable definition) {
		throwExceptionWhenSuiteDescriptionExists(description);
		this.focusedSuiteDefinitions.put(description, definition);
	}

	public void xdescribe(String description, PendingInvokable definition) {
		throwExceptionWhenSuiteDescriptionExists(description);
		this.suiteDefinitions.put(description, definition);
	}

	private void throwExceptionWhenSuiteDescriptionExists(final String description) {
		if (this.suiteDefinitions.containsKey(description) || this.focusedSuiteDefinitions.containsKey(description)) {
			throw new IllegalArgumentException(String.format("Suite with description '%s' does already exist", description));
		}
	}

	public void it(String description, Invokable definition) {
		throwExceptionWhenSpecDescriptionExists(description);
		this.specDefinitions.put(description, Optional.of(definition));
	}

	public void fit(String description, Invokable definition) {
		throwExceptionWhenSpecDescriptionExists(description);
		this.focusedSpecDefinitions.put(description, Optional.of(definition));
	}

	public void xit(String description) {
		throwExceptionWhenSpecDescriptionExists(description);
		this.specDefinitions.put(description, Optional.empty());
	}

	private void throwExceptionWhenSpecDescriptionExists(final String description) {
		if (this.specDefinitions.containsKey(description) || this.focusedSpecDefinitions.containsKey(description)) {
			throw new IllegalArgumentException(String.format("Spec with description '%s' does already exist", description));
		}
	}

	public void beforeEach(Invokable block) {
		this.beforeEachHandlers.add(block);
	}

	public void before(Invokable block) {
		this.beforeHandlers.add(block);
	}

	public void afterEach(Invokable block) {
		this.afterEachHandlers.add(block);
	}

	public void after(Invokable block) {
		this.afterHandlers.add(block);
	}

	public Map<String, Invokable> getSuiteDefinitions() {
		return suiteDefinitions;
	}

	public Map<String, Invokable> getFocusedSuiteDefinitions() {
		return focusedSuiteDefinitions;
	}

	public Map<String, Optional<Invokable>> getSpecDefinitions() {
		return specDefinitions;
	}

	public Map<String, Optional<Invokable>> getFocusedSpecDefinitions() {
		return focusedSpecDefinitions;
	}

	public List<Invokable> getBeforeEachHandlers() {
		return beforeEachHandlers;
	}

	public List<Invokable> getBeforeHandlers() {
		return beforeHandlers;
	}

	public List<Invokable> getAfterEachHandlers() {
		return afterEachHandlers;
	}

	public List<Invokable> getAfterHandlers() {
		return afterHandlers;
	}
}
