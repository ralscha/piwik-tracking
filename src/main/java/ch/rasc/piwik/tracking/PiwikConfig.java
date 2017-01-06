/**
 * Copyright 2016-2017 Ralph Schaer <ralphschaer@gmail.com>
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

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Immutable(copy = false)
@Value.Style(depluralize = true, visibility = ImplementationVisibility.PACKAGE)
public interface PiwikConfig {

	default String scheme() {
		return "https";
	}

	String host();

	default String path() {
		return "piwik.php";
	}

	String authToken();

	List<String> idSite();

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder extends ImmutablePiwikConfig.Builder {
		// nothing here
	}
}
