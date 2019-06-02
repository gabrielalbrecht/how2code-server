package com.recommendersystem.recommender.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.recommendersystem.recommender.models.User;
import com.recommendersystem.recommender.repository.UserRepository;
import com.recommendersystem.recommender.utils.StringUtil;

@CrossOrigin(origins = "http://localhost:8100")
@RestController
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository repository;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public Map<String, Object> getAllUsers() {
		Map<String, Object> response = new HashMap<>();

		response.put("users", repository.findAll());
		response.put("success", true);

		return response;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public Map<String, Object> getUserById(@PathVariable("id") String id) {
		Map<String, Object> response = new HashMap<>();

		response.put("user", repository.findById(id));
		response.put("success", true);

		return response;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	public Map<String, Object> modifyUserById(@PathVariable("id") ObjectId id, @Valid @RequestBody User user) {
		user.set_id(id);
		repository.save(user);

		Map<String, Object> response = new HashMap<>();

		response.put("user", user);
		response.put("success", true);

		return response;
	}

	@RequestMapping(value = "/", method = RequestMethod.POST)
	public Map<String, Object> createUser(@Valid @RequestBody User user) {
		Map<String, Object> response = new HashMap<>();

		if (StringUtil.isBlank(user.getEmail()) || StringUtil.isBlank(user.getPassword())) {
			response.put("success", false);
			response.put("message", "Email e senha são obrigatórios");

			return response;
		}

		if (!repository.findByEmail(user.getEmail()).isEmpty()) {
			response.put("success", false);
			response.put("message", "Email já está cadastrado");

			return response;
		}

		user.set_id(ObjectId.get());

		user.setSession(SessionController.createSession());
		repository.save(user);

		response.put("user", user);
		response.put("success", true);

		return response;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public Map<String, Object> deleteUser(@PathVariable String id) {
		Optional<User> userAux = repository.findById(id);

		Map<String, Object> response = new HashMap<>();

		if (!userAux.isPresent()) {
			response.put("message", "Usuário não encontrado!");
			response.put("success", "false");

			return response;
		}

		repository.delete(userAux.get());

		response.put("success", true);

		return response;
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public Map<String, Object> userLogin(@RequestBody User userForLogin) {
		Map<String, Object> response = new HashMap<>();

		List<User> users = repository.findByEmailAndPassword(userForLogin.getEmail(), userForLogin.getPassword());

		if (users.isEmpty()) {
			response.put("success", false);
			response.put("message", "Email ou senha incorretos!");

			return response;
		}

		User user = users.get(0);
		user.setSession(SessionController.createSession());
		repository.save(user);

		response.put("user", user);
		response.put("success", true);

		return response;
	}

	@RequestMapping(value = "/logout", method = RequestMethod.POST)
	public Map<String, Object> userLogout(@RequestBody User userForLogout) {
		Optional<User> userAux = repository.findById(userForLogout.getId());

		Map<String, Object> response = new HashMap<>();

		if (!userAux.isPresent()) {
			response.put("message", "Usuário não encontrado!");
			response.put("success", "false");

			return response;
		}

		User user = userAux.get();

		user.getSession().setSessionId("");
		repository.save(user);

		response.put("user", user);
		response.put("success", true);

		return response;
	}
}
