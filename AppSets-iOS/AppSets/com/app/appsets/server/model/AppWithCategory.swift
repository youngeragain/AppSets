//
//  AppWithCategory.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/15.
//

import Foundation

class AppWithCategory : Codable {
    let categoryName: String
    let categoryNameZh: String
    let applications: [Application]
}
