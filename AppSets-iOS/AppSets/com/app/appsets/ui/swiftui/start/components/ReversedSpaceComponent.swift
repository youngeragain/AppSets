//
//  ReversedSpaceComponent.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/24.
//

import SwiftUI

struct ReversedSpaceComponent: View {
    var body: some View {
        VStack{
            Color.secondary.clipShape(RoundedRectangle(cornerSize: CGSize(width: 12, height: 12)))
        }.frame(height: 200).background(.yellow).padding()
    }
}

#Preview {
    ReversedSpaceComponent()
}
