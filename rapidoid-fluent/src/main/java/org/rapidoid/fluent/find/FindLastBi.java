package org.rapidoid.fluent.find;

/*
 * #%L
 * rapidoid-fluent
 * %%
 * Copyright (C) 2014 - 2016 Nikolche Mihajlovski and contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.rapidoid.fluent.utils.Lambdas;
import org.rapidoid.fluent.utils.StreamUtils;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * @author Nikolche Mihajlovski
 * @since 5.0.0
 */
public class FindLastBi<K, V> {

	private final Map<K, V> items;

	public FindLastBi(Map<K, V> items) {
		this.items = items;
	}

	public Optional<Entry<K, V>> where(BiPredicate<K, V> predicate) {
		return StreamUtils.findLastOf(items.entrySet().stream().filter(Lambdas.entryTest(predicate)));
	}

}
