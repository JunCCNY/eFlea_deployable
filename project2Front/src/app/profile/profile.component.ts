import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';

import {User} from '../models/user';
import {HttpClient} from '@angular/common/http';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  user: User;
  url = 'http://ec2-18-217-30-101.us-east-2.compute.amazonaws.com:8085/account/verify';

  constructor(private router: Router,
              private http: HttpClient) {
    this.getProfile();
  }

  ngOnInit() {
    this.getProfile();
  }

  update() {
    this.router.navigate([('updateInfo')]);
  }

  getProfile() {
    this.http.get<User>(this.url, {withCredentials: true})
      .subscribe((data) => {
        console.log(data);
        this.user = data;
      }, (err) => {
        this.router.navigate([('')]);
      });
  }
}
