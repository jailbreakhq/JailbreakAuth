package org.jailbreak.service.db.dao;

import java.util.List;

import org.jailbreak.api.representations.Representations.User;
import org.jailbreak.service.db.BindProtobuf;
import org.jailbreak.service.db.mappers.UsersMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;

import com.google.common.base.Optional;

@RegisterMapper(UsersMapper.class)
public interface UsersDAO {

	@SqlQuery("SELECT * FROM users WHERE user_id = :user_id")
	@SingleValueResult(User.class)
	public Optional<User> getUser(@Bind("user_id") long user_id);
	
	@SqlQuery("SELECT * FROM users WHERE email = :email")
	@SingleValueResult(User.class)
	public Optional<User> getUserByEmail(@Bind("email") String email);
	
	@SqlQuery("SELECT * FROM users LIMIT 20")
	public List<User> getUsers();

	@SqlUpdate("INSERT INTO users VALUES(:user_id, UNIX_TIMESTAMP(UTC_TIMESTAMP()), :email, :user_level, :first_name, :last_name, :gender, :timezone, :locale, :facebook_link, :password)")
	@GetGeneratedKeys
	public int createUser(@BindProtobuf User user);

}