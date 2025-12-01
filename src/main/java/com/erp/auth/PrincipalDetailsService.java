package com.erp.auth;

import com.erp.dao.ManagerDAO;
import com.erp.dao.StoreDAO;
import com.erp.dto.ManagerDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {

    private final ManagerDAO managerDAO;
    private final StoreDAO storeDAO;

    @Override
    public UserDetails loadUserByUsername(String managerId) throws UsernameNotFoundException {


        ManagerDTO manager = managerDAO.getManagerForLogin(managerId);

        if (manager == null) {
            throw new UsernameNotFoundException("해당 아이디가 존재하지 않습니다: " + managerId);
        }

        PrincipalDetails pd = new PrincipalDetails(manager);
        if ("ROLE_STORE".equals(manager.getRole())) {
            Long storeNo = storeDAO.getStoreNoByManager(managerId);
            pd.setStoreNo(storeNo);
        }

        return pd;
    }
}
