package com.lsh.auth.repository;

import com.lsh.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/26 15:15
 * @Desc:
 */
@Repository
public interface UserRepository  extends JpaRepository<User,Integer> {
}
