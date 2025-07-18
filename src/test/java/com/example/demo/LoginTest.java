package com.example.demo;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;


// ポイント：@ExtendWith
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class LoginTest {

	@Autowired
	private MockMvc mockMvc;
	
	/** system ユーザーでログイン処理を実行 */
	@Test
	public void loginTestSystem() throws Exception {
		
		// ログイン処理を実行
		mockMvc
			.perform(post("http://localhost:8081/login")
					.with(csrf())
					.param("userId", "system")
					.param("password", "password")
					.param("tenantId", "tenant"))
			.andExpect(status().isFound())	// HTTPレスポンス
			.andExpect(authenticated()	//認証されたか
					.withUsername("system")
					.withRoles("ADMIN", "GENERAL"))
			.andExpect(redirectedUrl("/home"));	// リダイレクト先
			
	}
	
	/** sample1 ユーザーでログイン処理を実行 */
	@Test
	public void loginTestSample1() throws Exception {
		
		// ログイン処理を実行
		mockMvc
			.perform(post("http://localhost:8081/login")
					.with(csrf())
					.param("userId", "sample1")
					.param("password", "password")
					.param("tenantId", "tenant"))
			.andExpect(status().isFound())	// HTTPレスポンス
			.andExpect(authenticated()	//認証されたか
					.withUsername("sample1")
					.withRoles("GENERAL"))
			.andExpect(redirectedUrl("/password/change"));	// リダイレクト先
			
	}
	
	/** sample2 ユーザーでログイン処理を実行 */
	@Test
	public void loginTestSample2() throws Exception {
		
		// ログイン処理を実行
		mockMvc
			.perform(post("http://localhost:8081/login")
					.with(csrf())
					.param("userId", "sample2")
					.param("password", "password")
					.param("tenantId", "tenant"))
			.andExpect(status().isFound())	// HTTPレスポンス
			.andExpect(unauthenticated())	//認証されたか
//					.withUsername("sample2")
//					.withRoles("GENERAL"))
			.andExpect(redirectedUrl("/login?error"));	// リダイレクト先
			
	}
}
