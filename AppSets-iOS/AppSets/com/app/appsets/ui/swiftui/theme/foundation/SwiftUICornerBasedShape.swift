//
//  SwiftUICornerBasedShape.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/4.
//

struct SwiftUICornerBasedShape: SwiftUIShape {
    let topStart: Int
    let topEnd: Int
    let bottomStart: Int
    let bottomEnd: Int
    
    init(
        topStart: Int = 0,
        topEnd: Int = 0,
        bottomStart: Int = 0,
        bottomEnd: Int = 0
    ) {
        self.topStart = topStart
        self.topEnd = topEnd
        self.bottomStart = bottomStart
        self.bottomEnd = bottomEnd
    }
}
