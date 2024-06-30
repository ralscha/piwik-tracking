/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.rasc.piwik.tracking;

import java.util.List;
import java.util.Map;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Immutable
@Value.Style(depluralize = true, visibility = ImplementationVisibility.PACKAGE)
public interface PiwikRequest {

	String url();

	List<String> idSite();

	Map<QueryParameter, Object> parameters();

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder extends ImmutablePiwikRequest.Builder {
		// nothing here
	}
}
