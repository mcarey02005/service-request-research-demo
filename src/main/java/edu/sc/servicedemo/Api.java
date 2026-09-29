package edu.sc.servicedemo;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
class Api {
  final RequestRepo requests;
  final UserRepo users;
  Api(RequestRepo requests, UserRepo users) { this.requests = requests; this.users = users; }
  record Login(String username, String password) {}
  record RequestInput(String title, String details, LocalDate scheduledFor) {}
  record RequestView(Long id, String title, String details, String status, LocalDate scheduledFor) {}
  record UserView(Long id, String username) {}
  private RequestView view(ServiceRequest r) { return new RequestView(r.id, r.title, r.details, r.status, r.scheduledFor); }

  @PostMapping("/login") Map<String, String> login(@RequestBody Login login) {
    User user = users.findByUsername(login.username()).filter(u -> u.password.equals(login.password()))
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    return Map.of("token", "demo-jwt-token", "username", user.username);
  }
  @GetMapping("/requests") List<RequestView> all(@RequestParam(required = false) String status) {
    List<ServiceRequest> found = status == null ? requests.findAll() : requests.findByStatus(status);
    return found.stream().map(this::view).toList();
  }
  @GetMapping("/users") List<UserView> allUsers() { return users.findAll().stream().map(u -> new UserView(u.id, u.username)).toList(); }
  @PostMapping("/requests") RequestView add(@RequestBody RequestInput input) {
    ServiceRequest request = new ServiceRequest();
    request.title = input.title(); request.details = input.details(); request.scheduledFor = input.scheduledFor();
    request.user = users.findByUsername("demo").orElseThrow();
    return view(requests.save(request));
  }
  @PatchMapping("/requests/{id}/complete") RequestView complete(@PathVariable Long id) {
    ServiceRequest request = requests.findById(id).orElseThrow(); request.status = "COMPLETE";
    return view(requests.save(request));
  }
  @DeleteMapping("/requests/{id}") void delete(@PathVariable Long id) { requests.deleteById(id); }
}
