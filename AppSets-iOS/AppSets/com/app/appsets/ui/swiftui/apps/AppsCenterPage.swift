//
//  AppsCommponent.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/16.
//

import SwiftUI

struct AppsCenterPage: View {
    private static let TAG = "AppsCenterPage"
    
    @Environment(MainViewModel.self) var viewModel: MainViewModel

    let onBioClickListener: (any Bio) -> Void

    init(onBioClick: @escaping (any Bio) -> Void) {
        onBioClickListener = onBioClick
        PurpleLogger.current.d(AppsCenterPage.TAG, "init")
    }

    func ApplicationItem(_ application: Application) -> some View {
        VStack(alignment: .center, spacing: 12) {
            AsyncImage(
                url: URL(string: application.iconUrl ?? ""),
                content: { image in
                    image
                        .resizable()
                        .scaledToFit()
                        .frame(width: 68, height: 68, alignment: .center)
                        .clipShape(RoundedRectangle(cornerRadius: 24))
                },
                placeholder: {
                    RoundedRectangle(cornerRadius: 24)
                        .frame(width: 68, height: 68, alignment: .center)
                        .foregroundColor(Theme.colorSchema.outline)
                }
            )
            Text(application.name ?? "")
                .frame(maxWidth: 78)
                .font(.system(size: 12))
                .lineLimit(2)
        }.padding(.init(top: 12, leading: 12, bottom: 12, trailing: 12)).onTapGesture {
            onBioClickListener(application)
        }
    }

    var body: some View {
        VStack{
            let appsUseCase = viewModel.appsUseCase
            VStack {
                ScrollView(.vertical) {
                    Spacer().frame(height: 52)
                    VStack(spacing: 12) {
                        ForEach(appsUseCase.applications.indices, id: \.self) { vIndex in
                            ScrollView(.horizontal) {
                                HStack(spacing: 12) {
                                    let appCategory = appsUseCase.applications[vIndex]
                                    ForEach(appCategory.applications.indices, id: \.self) { hIndex in
                                        let application = appCategory.applications[hIndex]
                                        ApplicationItem(application)
                                    }
                                }.padding(.init(top: 4, leading: 12, bottom: 4, trailing: 12))
                            }.scrollIndicators(.hidden)
                        }
                    }
                    Spacer().frame(height: 128)
                }.scrollIndicators(.hidden)
            }
            .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity)
            .edgesIgnoringSafeArea(.all)
            .onAppear(perform: {
                appsUseCase.loadInitialData(LocalContext.current)
            })
        }
       
    }
}

#Preview {
    AppsCenterPage(
        onBioClick: { _ in
        }
    )
}
