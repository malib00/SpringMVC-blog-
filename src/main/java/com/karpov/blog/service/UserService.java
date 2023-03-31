package com.karpov.blog.service;

import com.karpov.blog.models.User;
import com.karpov.blog.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username);
		if (user == null) {
			throw new UsernameNotFoundException("User not found");
		}
		return user;
	}

	public void follow(User authenticatedUser, User user) {
		user.getFollowers().add(authenticatedUser);
		userRepository.save(user);
	}

	public void unfollow(User authenticatedUser, User user) {
		user.getFollowers().remove(authenticatedUser);
		userRepository.save(user);
	}

	public boolean sameUsernameFound(String username) {
		User userWithSameUsername = userRepository.findByUsername(username);
		if (userWithSameUsername != null) {
			return true;
		} else {
			return false;
		}
	}
}
