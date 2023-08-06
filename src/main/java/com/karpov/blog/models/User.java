package com.karpov.blog.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "usr")
public class User implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@NotBlank(message = "Please write your username.")
	@Size(min = 2, max = 20, message = "Username must be between 2 and 20 characters long.")
	@Pattern(regexp = "^[a-z0-9]*$", message = "Username can contain only lowercase letters (a-z) and numbers (0-9) without spaces.")
	private String username;

	@Pattern(regexp = "^[A-Za-z ]*$", message = "Full name can contain only letters (A-Z,a-z) and spaces.")
	private String fullname;

	private String about;

	private String password;

	private boolean active;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval=true)
	private ImageFile avatarImage;

	private Instant timestamp;

	@Email(message = "Email is not correct.")
	@NotBlank(message = "Please write your e-mail.")
	private String email;

	private String emailActivationCode;

	@OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Post> posts;

	@ManyToMany
	@JoinTable(
			name = "user_followers",
			joinColumns = {@JoinColumn(name = "channel_id")},
			inverseJoinColumns = {@JoinColumn(name = "follower")}
	)
	private Set<User> followers = new HashSet<>();

	@ManyToMany
	@JoinTable(
			name = "user_followers",
			joinColumns = {@JoinColumn(name = "follower")},
			inverseJoinColumns = {@JoinColumn(name = "channel_id")}
	)
	private Set<User> following = new HashSet<>();

	@ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
	@CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
	@Enumerated(EnumType.STRING)
	private Set<Role> roles;

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		User user = (User) obj;
		return Objects.equals(id, user.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return isActive();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return getRoles();
	}
}
