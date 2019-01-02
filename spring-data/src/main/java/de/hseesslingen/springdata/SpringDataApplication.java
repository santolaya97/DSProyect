package de.hseesslingen.springdata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.Collections;

@Configuration
@EnableWebSecurity
class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.anyRequest()
				.permitAll()
				.and().csrf().disable();
	}
}

@SpringBootApplication
@Controller
public class SpringDataApplication {

	@Autowired
	TodoRepository todoRepository;

	@Value("${spring.application.name}")
	private String appName;

	@Autowired
	private ApplicationProperties ap;

	@GetMapping("/value")
	String value(){
		return appName;
	}

	@GetMapping("/appNameViaPOJO")
	String appNameViaPOJO() {
		return this.ap.getName();
	}


	@GetMapping("/")
	public String homePage(Model model) {

		ArrayList<String> todos = new ArrayList<String>();
		todoRepository.findAll().forEach(todo -> todos.add(todo.getTodo()));
		//appName = "hostias";
		//model.addAttribute("appName", appName);
		model.addAttribute("vector", todos.toString());
		return "home.html";
	}

	//@GetMapping("/add/{todo}")
	@RequestMapping(value="add", method = RequestMethod.GET)
	//@ResponseBody
	String addTodo(Model model, @RequestParam("word") String todo){
		model.addAttribute("wordAdd", todo);
		todoRepository.save(new Todo(todo));
		String wordAdd = todo;
		return "add.html";
	}

	@GetMapping("/remove")
	String removeTodo(Model model) {
		ArrayList<String> todos = new ArrayList<String>();
		todoRepository.findAll().forEach(todo -> todos.add(todo.getTodo()));
		model.addAttribute("vector", todos.toString());
		return "remove.html";

	}

	//@GetMapping("/remove/{todo}")
	@RequestMapping(value="removeWord", method = RequestMethod.GET)
	String removeTodo(Model model, @RequestParam("wordRemove") String todo) {
		if (todoRepository.existsById(todo)) {
			todoRepository.deleteById(todo);
		} else  {
			model.addAttribute("message", "The word doesn't exist in the vector");
		}
		ArrayList<String> todos = new ArrayList<String>();
		todoRepository.findAll().forEach(i -> todos.add(i.getTodo()));
		model.addAttribute("vector", todos.toString());
		return "remove.html ";
	}



	@GetMapping("/find")
	String find(Model model) {
		ArrayList<String> todos = new ArrayList<String>();
		todoRepository.findAll().forEach(i -> todos.add(i.getTodo()));
		model.addAttribute("vector", todos.toString());
		return "find.html";
	}

	public boolean isInteger( String input )
	{	try {
			Integer.parseInt( input );
			return true;
		}catch( Exception e){
			return false;
		}
	}

	@RequestMapping(value="findWord", method = RequestMethod.GET)
	String find(Model model,@RequestParam("wordFind") String todo) {
		ArrayList<String> todos = new ArrayList<String>();
		todoRepository.findAll().forEach(i -> todos.add(i.getTodo()));
		model.addAttribute("vector", todos.toString());
		if (isInteger(todo) && Integer.parseInt(todo)>0 && Integer.parseInt(todo)<=todoRepository.count()) {
			model.addAttribute("findWord", (todos.get(Integer.parseInt(todo) - 1)));
		} else {
			model.addAttribute("findWord", "You have to introduce a numer in the range [1.."+
					String.valueOf(todoRepository.count())+"]");
		}
		return "find.html";
	}

	@GetMapping("/findIndex")
	String findIndex(Model model) {
		ArrayList<String> todos = new ArrayList<String>();
		todoRepository.findAll().forEach(i -> todos.add(i.getTodo()));
		model.addAttribute("vector", todos.toString());
		return "findIndex.html";
	}

	@RequestMapping(value="findWordIndex", method = RequestMethod.GET)
	String findIndex(Model model, @RequestParam("wordFindIndex") String todo) {
		ArrayList<String> todos = new ArrayList<String>();
		final int[] index = new int[1];
		final int[] j = {0};
		todoRepository.findAll().forEach(i ->{ todos.add(i.getTodo()); if(i.getTodo().equals(todo)){
			index[0] = j[0];};
			j[0]++;});
		model.addAttribute("vector", todos.toString());
		if (todoRepository.existsById(todo)) {
			model.addAttribute("wordFindIndex", index[0]+1);
		} else {
			model.addAttribute("wordFindIndex", "The word doesn´t exist in the vector");
		}
		return "findIndex.html";
	}

	@GetMapping("/reverseOrder")
	String order(Model model) {
		ArrayList<String> todos = new ArrayList<String>();
		todoRepository.findAll().forEach(todo -> todos.add(todo.getTodo()));
		todos.sort(Collections.reverseOrder());
		model.addAttribute("vector", todos.toString());
		return "home.html";
	}

	@GetMapping("/removeAll")
	String removeAll(Model model){
		todoRepository.deleteAll();
		ArrayList<String> todos = new ArrayList<String>();
		todoRepository.findAll().forEach(i -> todos.add(i.getTodo()));
		model.addAttribute("vector", todos.toString());
		return "home.html";
	}

	public static void main(String[] args) {SpringApplication.run(SpringDataApplication.class, args);}
}

@Component
@ConfigurationProperties("application")
class ApplicationProperties {

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}

@Entity
class Todo{

	@Id
	String todo;

	public Todo(){}

	public Todo(String todo){ 
		this.todo = todo; 
	}

	public String getTodo(){ 
		return todo; 
	}

	public void setTodo(String todo) { 
		this.todo = todo; 
	}

}

interface TodoRepository extends CrudRepository<Todo, String> {
 
}
