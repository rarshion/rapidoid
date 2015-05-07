package org.rapidoidx.db;

/*
 * #%L
 * rapidoid-x-db-tests
 * %%
 * Copyright (C) 2014 - 2015 Nikolche Mihajlovski
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import org.rapidoid.annotation.Authors;
import org.rapidoid.annotation.Since;
import org.rapidoid.util.U;
import org.rapidoidx.db.DB;
import org.rapidoidx.db.model.IPost;
import org.rapidoidx.db.model.IProfile;
import org.testng.annotations.Test;

@Authors("Nikolche Mihajlovski")
@Since("3.0.0")
public class DbInterfaceRelationsTest extends DbTestCommons {

	@Test
	public void testInverseRelations1() {

		IProfile profile = DB.entity(IProfile.class);

		IPost post1 = DB.entity(IPost.class);
		post1.content().set("post 1");

		IPost post2 = DB.entity(IPost.class);
		post2.content().set("post 2");

		profile.posts().add(post1);
		profile.posts().add(post2);

		DB.persist(profile);
		DB.refresh(post1);
		DB.refresh(post2);

		eq(profile.posts(), U.list(post1, post2));
		eq(post1.postedOn().get(), profile);
		eq(post2.postedOn().get(), profile);

		profile.posts().remove(post1);

		DB.persist(profile);
		DB.refresh(post1);
		DB.refresh(post2);

		eq(profile.posts(), U.list(post2));
		isNull(post1.postedOn().get());
		eq(post2.postedOn().get(), profile);

		DB.shutdown();
	}

	@Test
	public void testInverseRelations2() {

		IProfile profile = DB.entity(IProfile.class);

		IPost post1 = DB.entity(IPost.class);
		post1.content().set("post 1");

		IPost post2 = DB.entity(IPost.class);
		post2.content().set("post 2");

		post1.postedOn().set(profile);
		post2.postedOn().set(profile);

		DB.persist(post1);
		DB.persist(post2);
		DB.refresh(profile);

		eq(profile.posts(), U.list(post1, post2));
		eq(post1.postedOn().get(), profile);
		eq(post2.postedOn().get(), profile);

		post1.postedOn().set(null);

		DB.persist(post1);
		DB.refresh(profile);
		DB.refresh(post2);

		eq(profile.posts(), U.list(post2));
		isNull(post1.postedOn().get());
		eq(post2.postedOn().get(), profile);

		DB.shutdown();
	}

}