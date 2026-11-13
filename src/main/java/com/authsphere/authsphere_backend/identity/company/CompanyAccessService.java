
package com.authsphere.authsphere_backend.identity.company;

import com.authsphere.authsphere_backend.identity.user.User;
import org.springframework.stereotype.Service;

@Service
public class CompanyAccessService {

    public void validateCompanyAccess(User user) {

        // SuperAdmin accounts normally do not belong to a company.
        if (user.getCompany() == null) {
            return;
        }

        Company company = user.getCompany();

        if (company.isDeleted()
                || company.getStatus() != CompanyStatus.ACTIVE) {

            throw new IllegalStateException(
                    "Access denied. Your company is not active."
            );
        }
    }
}
